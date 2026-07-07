package com.jksalcedo.tend.data.contacts

import android.content.ContentUris
import android.content.Context
import android.provider.ContactsContract
import com.jksalcedo.tend.domain.model.NativeContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class NativeContactsDataSource(private val context: Context) {

    suspend fun queryContacts(): List<NativeContact> = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val contacts = mutableListOf<NativeContact>()

        val cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.PHOTO_URI,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            ),
            null,
            null,
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val lookupKeyIndex = it.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)
            val nameIndex = it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val photoUriIndex = it.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI)
            val hasPhoneIndex = it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)

            while (it.moveToNext()) {
                val contactId = it.getLong(idIndex)
                val lookupKey = it.getString(lookupKeyIndex) ?: continue
                val name = it.getString(nameIndex) ?: continue
                val photoUri = it.getString(photoUriIndex)
                val hasPhone = it.getInt(hasPhoneIndex) > 0

                contacts.add(
                    NativeContact(
                        lookupKey = lookupKey,
                        contactId = contactId,
                        name = name,
                        phoneNumber = if (hasPhone) queryFirstPhoneNumber(contactId) else null,
                        email = queryFirstEmail(contactId),
                        photoUri = photoUri
                    )
                )
            }
        }

        contacts
    }

    // Resolves a previously-linked contact via its durable LOOKUP_KEY rather than the cached
    // _ID, which Android's own aggregation can change (merge/split) without the contact having
    // been deleted. Contacts.lookupContact() transparently follows that reorganization; a null
    // result means the contact is genuinely gone, not merely renumbered.
    suspend fun resolveContact(lookupKey: String, cachedContactId: Long?): NativeContact? =
        withContext(Dispatchers.IO) {
            val lookupUri = ContactsContract.Contacts.getLookupUri(cachedContactId ?: -1L, lookupKey)
            val resolvedUri = ContactsContract.Contacts.lookupContact(context.contentResolver, lookupUri)
                ?: return@withContext null

            context.contentResolver.query(
                resolvedUri,
                arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                    ContactsContract.Contacts.PHOTO_URI,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER
                ),
                null,
                null,
                null
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null

                val contactId = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val resolvedLookupKey = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)
                ) ?: return@use null
                val name = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                ) ?: return@use null
                val photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))
                val hasPhone = cursor.getInt(
                    cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                ) > 0

                NativeContact(
                    lookupKey = resolvedLookupKey,
                    contactId = contactId,
                    name = name,
                    phoneNumber = if (hasPhone) queryFirstPhoneNumber(contactId) else null,
                    email = queryFirstEmail(contactId),
                    photoUri = photoUri
                )
            }
        }

    // Copies the native contact's photo into app-private storage so it keeps displaying even if
    // READ_CONTACTS is later revoked (a content:// URI reference would go unreadable then). Returns
    // null if the contact has no photo; the caller should keep whatever was cached previously.
    suspend fun cachePhoto(contactId: Long): String? = withContext(Dispatchers.IO) {
        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val stream = ContactsContract.Contacts.openContactPhotoInputStream(
            context.contentResolver,
            contactUri,
            true
        ) ?: return@withContext null

        val dir = File(context.filesDir, "contact_photos").apply { mkdirs() }
        val file = File(dir, "$contactId.jpg")
        stream.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file.absolutePath
    }

    // TODO: Tend's Person model stores a single phoneNumber/email (see domain/model/Person.kt),
    // but a native contact can have several of each. Until Tend supports multiple values per
    // person, we pick the contact's own designated default (IS_SUPER_PRIMARY) when one exists,
    // falling back to an arbitrary row otherwise — any other numbers/emails are silently dropped.
    private fun queryFirstPhoneNumber(contactId: Long): String? {
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            "${ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY} DESC"
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
            }
        }
        return null
    }

    private fun queryFirstEmail(contactId: Long): String? {
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            "${ContactsContract.CommonDataKinds.Email.IS_SUPER_PRIMARY} DESC"
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS)
                )
            }
        }
        return null
    }
}

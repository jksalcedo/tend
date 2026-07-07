package com.jksalcedo.tend.data.contacts

import android.content.Context
import android.provider.ContactsContract
import com.jksalcedo.tend.domain.model.NativeContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

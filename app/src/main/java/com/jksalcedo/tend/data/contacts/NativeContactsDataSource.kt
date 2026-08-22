package com.jksalcedo.tend.data.contacts

import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.ContactsContract
import androidx.core.net.toUri
import com.jksalcedo.tend.domain.model.EventType
import com.jksalcedo.tend.domain.model.NativeContact
import com.jksalcedo.tend.domain.model.PersonEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
                        photoUri = photoUri,
                        events = queryEvents(contactId)
                    )
                )
            }
        }

        contacts
    }

    // Resolves a contact via its durable LOOKUP_KEY rather than the cached _ID, 
    // which can change due to Android's internal contact aggregation.
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
                    photoUri = photoUri,
                    events = queryEvents(contactId)
                )
            }
        }

    // Atomically creates a brand-new, local, unsynced raw contact.
    suspend fun createContact(
        name: String,
        phoneNumber: String?,
        email: String?,
        photoUri: String?
    ): NativeContact = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        // Read outside the batch; failure here shouldn't block contact creation.
        val photoBytes = if (!photoUri.isNullOrBlank()) {
            runCatching { resolver.openInputStream(photoUri.toUri())?.use { it.readBytes() } }.getOrNull()
        } else null

        val ops = ArrayList<ContentProviderOperation>()

        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .build()
        )

        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build()
        )

        if (!phoneNumber.isNullOrBlank()) {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY, 1)
                    .build()
            )
        }

        if (!email.isNullOrBlank()) {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                    .withValue(ContactsContract.CommonDataKinds.Email.IS_PRIMARY, 1)
                    .build()
            )
        }

        if (photoBytes != null) {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBytes)
                    .build()
            )
        }

        val results = resolver.applyBatch(ContactsContract.AUTHORITY, ops)
        val rawContactUri = results.firstOrNull()?.uri ?: error("Failed to create raw contact")
        val rawContactId = ContentUris.parseId(rawContactUri)

        val contactId = resolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(ContactsContract.RawContacts.CONTACT_ID),
            "${ContactsContract.RawContacts._ID} = ?",
            arrayOf(rawContactId.toString()),
            null
        )?.use { cursor ->
            // Cursor.getLong() on a SQL-NULL column returns 0. Check isNull explicitly 
            // to avoid aggregation lag resolving to contact ID 0.
            if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getLong(0) else null
        } ?: rawContactId

        val lookupKey = resolver.query(
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId),
            arrayOf(ContactsContract.Contacts.LOOKUP_KEY),
            null,
            null,
            null
        )?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
            ?: error("Newly created contact has no lookup key")

        NativeContact(
            lookupKey = lookupKey,
            contactId = contactId,
            name = name,
            phoneNumber = phoneNumber,
            email = email,
            photoUri = photoUri
        )
    }

    // Copies photo to private storage to survive READ_CONTACTS revocation.
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

    // Fetches the IS_SUPER_PRIMARY default, dropping others to maintain Tend's single-entry model.
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

    private fun queryEvents(contactId: Long): List<PersonEvent> {
        val events = mutableListOf<PersonEvent>()
        context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Event.START_DATE,
                ContactsContract.CommonDataKinds.Event.TYPE,
                ContactsContract.CommonDataKinds.Event.LABEL
            ),
            "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
            arrayOf(
                contactId.toString(),
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
            ),
            null
        )?.use { cursor ->
            val startDateIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.START_DATE)
            val typeIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.TYPE)
            val labelIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.LABEL)

            while (cursor.moveToNext()) {
                val startDateStr = cursor.getString(startDateIndex) ?: continue
                val eventTypeInt = cursor.getInt(typeIndex)
                val customLabel = cursor.getString(labelIndex)

                val parsedDate = parseEventDateToUtcMillis(startDateStr) ?: continue

                val (type, label) = when (eventTypeInt) {
                    ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY ->
                        EventType.BIRTHDAY to (customLabel?.takeIf { it.isNotBlank() } ?: "Birthday")
                    ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY ->
                        EventType.ANNIVERSARY to (customLabel?.takeIf { it.isNotBlank() } ?: "Anniversary")
                    else ->
                        EventType.OTHER to (customLabel?.takeIf { it.isNotBlank() } ?: "Other")
                }

                events.add(
                    PersonEvent(
                        label = label,
                        date = parsedDate,
                        type = type,
                        leadTimeDays = 0
                    )
                )
            }
        }
        return events
    }

    private fun parseEventDateToUtcMillis(dateStr: String): Long? {
        val trimmed = dateStr.trim()
        if (trimmed.isEmpty()) return null

        if (trimmed.startsWith("--")) {
            val parts = trimmed.removePrefix("--").split("-")
            if (parts.size == 2) {
                val month = parts[0].toIntOrNull() ?: return null
                val day = parts[1].toIntOrNull() ?: return null
                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    clear()
                    set(Calendar.YEAR, 1970)
                    set(Calendar.MONTH, month - 1)
                    set(Calendar.DAY_OF_MONTH, day)
                }
                return cal.timeInMillis
            }
        }

        val formats = arrayOf(
            "yyyy-MM-dd",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy/MM/dd",
            "yyyyMMdd",
            "MM/dd/yyyy",
            "MM-dd-yyyy"
        )

        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                    isLenient = false
                }
                val date = sdf.parse(trimmed)
                if (date != null) {
                    return date.time
                }
            } catch (_: Exception) {
            }
        }
        return null
    }
}

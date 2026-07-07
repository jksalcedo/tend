package com.jksalcedo.tend.domain.repository

import com.jksalcedo.tend.domain.model.NativeContact

interface ContactsRepository {
    suspend fun getImportableContacts(): List<NativeContact>
    suspend fun resolveContact(lookupKey: String, cachedContactId: Long?): NativeContact?
    suspend fun cachePhoto(contactId: Long): String?
}

package com.jksalcedo.tend.data.repository

import com.jksalcedo.tend.data.contacts.NativeContactsDataSource
import com.jksalcedo.tend.data.local.CheckInDao
import com.jksalcedo.tend.domain.model.NativeContact
import com.jksalcedo.tend.domain.repository.ContactsRepository

class ContactsRepositoryImpl(
    private val nativeContactsDataSource: NativeContactsDataSource,
    private val dao: CheckInDao
) : ContactsRepository {

    override suspend fun getImportableContacts(): List<NativeContact> {
        val linkedLookupKeys = dao.getLinkedLookupKeys().toSet()
        return nativeContactsDataSource.queryContacts()
            .filter { it.lookupKey !in linkedLookupKeys }
    }
}

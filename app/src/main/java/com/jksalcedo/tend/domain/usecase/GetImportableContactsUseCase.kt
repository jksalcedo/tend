package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.NativeContact
import com.jksalcedo.tend.domain.repository.ContactsRepository

class GetImportableContactsUseCase(
    private val contactsRepository: ContactsRepository
) {
    suspend operator fun invoke(): List<NativeContact> {
        return contactsRepository.getImportableContacts()
    }
}

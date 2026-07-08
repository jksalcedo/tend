package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.repository.ContactsRepository
import com.jksalcedo.tend.domain.repository.PersonRepository

// Promotes a Tend-only (Case 2) person to device-linked (Case 1) by creating a brand-new
// native contact and recording the link — never searches for or matches an existing native
// contact (see the README's "Non-Goals").
class SyncToDeviceUseCase(
    private val personRepository: PersonRepository,
    private val contactsRepository: ContactsRepository
) {
    suspend operator fun invoke(id: Long) {
        val person = personRepository.getPersonById(id) ?: return
        val created = contactsRepository.createContact(
            name = person.name,
            phoneNumber = person.phoneNumber,
            email = person.email,
            photoUri = person.photoUri
        )
        personRepository.updatePerson(
            person.copy(
                nativeLookupKey = created.lookupKey,
                nativeContactId = created.contactId,
                isDeviceLinkBroken = false
            )
        )
    }
}

package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.ContactsRepository
import com.jksalcedo.tend.domain.repository.PersonRepository

class RefreshLinkedContactsUseCase(
    private val contactsRepository: ContactsRepository,
    private val personRepository: PersonRepository
) {
    suspend operator fun invoke() {
        val linkedPeople = personRepository.getLinkedPeople()

        for (person in linkedPeople) {
            val lookupKey = person.nativeLookupKey ?: continue
            val resolved = contactsRepository.resolveContact(lookupKey, person.nativeContactId)

            if (resolved == null) {
                if (!person.isDeviceLinkBroken) {
                    personRepository.updatePerson(person.copy(isDeviceLinkBroken = true))
                }
                continue
            }

            val localPhotoPath = contactsRepository.cachePhoto(resolved.contactId) ?: person.localPhotoPath
            personRepository.updatePerson(
                person.copy(
                    name = resolved.name,
                    phoneNumber = resolved.phoneNumber,
                    email = resolved.email,
                    photoUri = resolved.photoUri,
                    localPhotoPath = localPhotoPath,
                    nativeContactId = resolved.contactId,
                    nativeLookupKey = resolved.lookupKey,
                    isDeviceLinkBroken = false
                )
            )
        }

        updateDuplicateFlags()
    }

    private suspend fun updateDuplicateFlags() {
        val refreshed = personRepository.getLinkedPeople()
        val byLookupKey: Map<String?, List<Person>> = refreshed.groupBy { it.nativeLookupKey }

        for (group in byLookupKey.values) {
            if (group.size > 1) {
                for (person in group) {
                    val otherId = group.first { it.id != person.id }.id
                    if (person.duplicateOfPersonId != otherId) {
                        personRepository.updatePerson(person.copy(duplicateOfPersonId = otherId))
                    }
                }
            } else {
                val person = group.first()
                if (person.duplicateOfPersonId != null) {
                    personRepository.updatePerson(person.copy(duplicateOfPersonId = null))
                }
            }
        }
    }
}

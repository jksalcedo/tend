package com.jksalcedo.tend.domain.usecase

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

            // Re-fetch right before writing rather than reusing the pre-await snapshot,
            // and bail if the person's link changed while we were awaiting the (slow)
            // ContentResolver round-trip above — e.g. the user unlinked this exact person
            // mid-refresh. Without this, a stale .copy() would silently overwrite whatever
            // they just did (Room's @Update replaces the whole row).
            val current = personRepository.getPersonById(person.id) ?: continue
            if (current.nativeLookupKey != lookupKey) continue

            if (resolved == null) {
                if (!current.isDeviceLinkBroken) {
                    personRepository.updatePerson(current.copy(isDeviceLinkBroken = true))
                }
                continue
            }

            val localPhotoPath = contactsRepository.cachePhoto(resolved.contactId) ?: current.localPhotoPath
            personRepository.updatePerson(
                current.copy(
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
    }
}

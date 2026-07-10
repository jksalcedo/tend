package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.repository.ContactsRepository
import com.jksalcedo.tend.domain.repository.PersonRepository
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Promotes a Tend-only (Case 2) person to device-linked (Case 1) by creating a brand-new
// native contact and recording the link — never searches for or matches an existing native
// contact (see the README's "Non-Goals").
class SyncToDeviceUseCase(
    private val personRepository: PersonRepository,
    private val contactsRepository: ContactsRepository
) {
    // Keyed by person id so different people can sync concurrently. Guards against two
    // concurrent invocations for the SAME person both passing the nativeLookupKey check before
    // either write completes, which would create two native contacts and orphan one. This is
    // only a structural guarantee because this class is registered as a Koin `single` — every
    // caller shares this same map.
    private val locksByPersonId = ConcurrentHashMap<Long, Mutex>()

    suspend operator fun invoke(id: Long) {
        val lock = locksByPersonId.getOrPut(id) { Mutex() }
        lock.withLock {
            val person = personRepository.getPersonById(id) ?: return
            if (person.nativeLookupKey != null) return
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
}

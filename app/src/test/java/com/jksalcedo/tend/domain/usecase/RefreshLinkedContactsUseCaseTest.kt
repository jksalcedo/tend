package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.NativeContact
import com.jksalcedo.tend.domain.model.Person
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RefreshLinkedContactsUseCaseTest {

    private fun person(
        name: String,
        nativeLookupKey: String? = null,
        nativeContactId: Long? = null
    ) = Person(
        name = name,
        photoUri = null,
        frequencyDays = 14,
        lastContactedAt = 0L,
        nextReminderAt = 0L,
        nativeLookupKey = nativeLookupKey,
        nativeContactId = nativeContactId
    )

    @Test
    fun `never reads or writes a Tend-only (unlinked) person`() = runBlocking {
        val personRepository = FakePersonRepository()
        val seeded = personRepository.seed(person("Marco"))
        val contactsRepository = FakeContactsRepository()
        val useCase = RefreshLinkedContactsUseCase(contactsRepository, personRepository)

        useCase()

        assertTrue(contactsRepository.resolveCalls.isEmpty())
        assertTrue(contactsRepository.cachePhotoCalls.isEmpty())
        assertEquals(seeded, personRepository.getPersonById(seeded.id))
    }

    @Test
    fun `refreshes identity fields for a linked person that still resolves`() = runBlocking {
        val personRepository = FakePersonRepository()
        val seeded = personRepository.seed(person("Priya", nativeLookupKey = "key-1", nativeContactId = 1L))
        val resolved = NativeContact(
            lookupKey = "key-1",
            contactId = 1L,
            name = "Priya Updated",
            phoneNumber = "555-0111",
            email = "priya@example.com",
            photoUri = null
        )
        val contactsRepository = FakeContactsRepository(resolvedContacts = mapOf("key-1" to resolved))
        val useCase = RefreshLinkedContactsUseCase(contactsRepository, personRepository)

        useCase()

        val updated = personRepository.getPersonById(seeded.id)
        assertEquals("Priya Updated", updated?.name)
        assertEquals("555-0111", updated?.phoneNumber)
        assertEquals(false, updated?.isDeviceLinkBroken)
    }

    @Test
    fun `flags a broken link when the lookup key no longer resolves`() = runBlocking {
        val personRepository = FakePersonRepository()
        val seeded = personRepository.seed(person("Priya", nativeLookupKey = "key-1", nativeContactId = 1L))
        val contactsRepository = FakeContactsRepository(resolvedContacts = mapOf("key-1" to null))
        val useCase = RefreshLinkedContactsUseCase(contactsRepository, personRepository)

        useCase()

        val updated = personRepository.getPersonById(seeded.id)
        assertEquals(true, updated?.isDeviceLinkBroken)
    }

    @Test
    fun `flags two people as duplicates when they resolve to the same lookup key`() = runBlocking {
        val personRepository = FakePersonRepository()
        val first = personRepository.seed(person("Priya A", nativeLookupKey = "shared-key", nativeContactId = 1L))
        val second = personRepository.seed(person("Priya B", nativeLookupKey = "shared-key", nativeContactId = 1L))
        val resolved = NativeContact(
            lookupKey = "shared-key",
            contactId = 1L,
            name = "Priya",
            phoneNumber = null,
            email = null,
            photoUri = null
        )
        val contactsRepository = FakeContactsRepository(resolvedContacts = mapOf("shared-key" to resolved))
        val useCase = RefreshLinkedContactsUseCase(contactsRepository, personRepository)

        useCase()

        val updatedFirst = personRepository.getPersonById(first.id)
        val updatedSecond = personRepository.getPersonById(second.id)
        assertEquals(second.id, updatedFirst?.duplicateOfPersonId)
        assertEquals(first.id, updatedSecond?.duplicateOfPersonId)
    }

    @Test
    fun `clears a stale duplicate flag once the collision is gone`() = runBlocking {
        val personRepository = FakePersonRepository()
        val seeded = personRepository.seed(
            person("Priya", nativeLookupKey = "key-1", nativeContactId = 1L).copy(duplicateOfPersonId = 42L)
        )
        val resolved = NativeContact(
            lookupKey = "key-1",
            contactId = 1L,
            name = "Priya",
            phoneNumber = null,
            email = null,
            photoUri = null
        )
        val contactsRepository = FakeContactsRepository(resolvedContacts = mapOf("key-1" to resolved))
        val useCase = RefreshLinkedContactsUseCase(contactsRepository, personRepository)

        useCase()

        val updated = personRepository.getPersonById(seeded.id)
        assertNull(updated?.duplicateOfPersonId)
    }
}

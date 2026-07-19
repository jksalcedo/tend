package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.NativeContact
import com.jksalcedo.tend.domain.model.Person
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class SyncToDeviceUseCaseTest {

    private fun tendOnlyPerson(name: String = "Marco") = Person(
        name = name,
        photoUri = null,
        phoneNumber = "555-0200",
        email = "marco@example.com",
        frequencyDays = 14,
        lastContactedAt = 0L,
        nextReminderAt = 0L
    )

    @Test
    fun `creates a new native contact with the person's fields`() = runBlocking {
        val personRepository = FakePersonRepository()
        val seeded = personRepository.seed(tendOnlyPerson())
        val contactsRepository = FakeContactsRepository()
        val useCase = SyncToDeviceUseCase(personRepository, contactsRepository)

        useCase(seeded.id)

        assertEquals(1, contactsRepository.createContactCalls.size)
        val call = contactsRepository.createContactCalls.first()
        assertEquals(seeded.name, call.name)
        assertEquals(seeded.phoneNumber, call.phoneNumber)
        assertEquals(seeded.email, call.email)
        assertEquals(seeded.photoUri, call.photoUri)
    }

    @Test
    fun `links the person to the newly created native contact`() = runBlocking {
        val personRepository = FakePersonRepository()
        val seeded = personRepository.seed(tendOnlyPerson())
        val createdContact = NativeContact(
            lookupKey = "marco-lookup-key",
            contactId = 77L,
            name = seeded.name,
            phoneNumber = seeded.phoneNumber,
            email = seeded.email,
            photoUri = null
        )
        val contactsRepository = FakeContactsRepository(createdContact = createdContact)
        val useCase = SyncToDeviceUseCase(personRepository, contactsRepository)

        useCase(seeded.id)

        val updated = personRepository.getPersonById(seeded.id)
        assertEquals("marco-lookup-key", updated?.nativeLookupKey)
        assertEquals(77L, updated?.nativeContactId)
        assertFalse(updated!!.isDeviceLinkBroken)
    }

    @Test
    fun `does nothing when the person is already linked`() = runBlocking {
        // Guards against a double-invocation (e.g. a rapid double-tap before the UI
        // disables the button) creating a second, orphaned native contact.
        val personRepository = FakePersonRepository()
        val seeded = personRepository.seed(
            tendOnlyPerson().copy(nativeLookupKey = "existing-key", nativeContactId = 5L)
        )
        val contactsRepository = FakeContactsRepository()
        val useCase = SyncToDeviceUseCase(personRepository, contactsRepository)

        useCase(seeded.id)

        assertEquals(0, contactsRepository.createContactCalls.size)
        assertEquals("existing-key", personRepository.getPersonById(seeded.id)?.nativeLookupKey)
    }

    @Test
    fun `two concurrent invocations for the same person only create one native contact`() = runBlocking {
        // The use case's own guard (not just the UI's disabled-button state) must hold up
        // against genuine concurrency, not just sequential double-taps on one ViewModel.
        val personRepository = FakePersonRepository()
        val seeded = personRepository.seed(tendOnlyPerson())
        var createCallCount = 0
        val contactsRepository = object : com.jksalcedo.tend.domain.repository.ContactsRepository {
            override suspend fun getImportableContacts(): List<NativeContact> = emptyList()
            override suspend fun resolveContact(lookupKey: String, cachedContactId: Long?): NativeContact? =
                null

            override suspend fun cachePhoto(contactId: Long): String? = null
            override suspend fun createContact(
                name: String,
                phoneNumber: String?,
                email: String?,
                photoUri: String?
            ): NativeContact {
                createCallCount++
                delay(10)
                return NativeContact(
                    lookupKey = "new-lookup-key",
                    contactId = 999L,
                    name = name,
                    phoneNumber = phoneNumber,
                    email = email,
                    photoUri = photoUri
                )
            }
        }
        val useCase = SyncToDeviceUseCase(personRepository, contactsRepository)

        val first = async { useCase(seeded.id) }
        val second = async { useCase(seeded.id) }
        first.await()
        second.await()

        assertEquals(1, createCallCount)
        assertEquals("new-lookup-key", personRepository.getPersonById(seeded.id)?.nativeLookupKey)
    }

    @Test
    fun `does nothing when the person does not exist`() = runBlocking {
        val personRepository = FakePersonRepository()
        val contactsRepository = FakeContactsRepository()
        val useCase = SyncToDeviceUseCase(personRepository, contactsRepository)

        useCase(id = 999L)

        assertEquals(0, contactsRepository.createContactCalls.size)
    }

    @Test
    fun `person created via Add Person always starts unlinked`() = runBlocking {
        val personRepository = FakePersonRepository()
        val useCase = AddPersonUseCase(personRepository)

        useCase(name = "Marco", frequencyDays = 14)

        val created = personRepository.insertedPeople.first()
        assertNull(created.nativeLookupKey)
        assertNull(created.nativeContactId)
    }
}

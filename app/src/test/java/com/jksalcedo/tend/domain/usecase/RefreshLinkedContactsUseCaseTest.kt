package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.NativeContact
import com.jksalcedo.tend.domain.model.Person
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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
    fun `does not overwrite a person who was unlinked while their refresh was in flight`() = runBlocking {
        // Regression test: the loop reads a Person snapshot, then awaits resolveContact/
        // cachePhoto (real I/O in production). If something else — e.g. UnlinkPersonUseCase —
        // changes this exact person's link in that window, the refresh must not clobber it
        // with a stale .copy() once its own await resolves.
        val personRepository = FakePersonRepository()
        val seeded = personRepository.seed(person("Priya", nativeLookupKey = "key-1", nativeContactId = 1L))
        val resolved = NativeContact(
            lookupKey = "key-1",
            contactId = 1L,
            name = "Priya Updated",
            phoneNumber = null,
            email = null,
            photoUri = null
        )
        val contactsRepository = object : ContactsRepositoryForRaceTest(resolved) {
            override suspend fun resolveContact(lookupKey: String, cachedContactId: Long?): NativeContact? {
                // Simulate a concurrent unlink happening between the initial read and this
                // (slow, real-world) resolve call completing.
                personRepository.updatePerson(seeded.copy(nativeLookupKey = null, nativeContactId = null))
                return super.resolveContact(lookupKey, cachedContactId)
            }
        }
        val useCase = RefreshLinkedContactsUseCase(contactsRepository, personRepository)

        useCase()

        val current = personRepository.getPersonById(seeded.id)
        assertEquals(null, current?.nativeLookupKey)
        assertEquals("Priya", current?.name)
    }

    @Test
    fun `does not overwrite a person who was unlinked while cachePhoto was in flight`() = runBlocking {
        // Regression test for the second half of the same race: resolveContact could also
        // complete cleanly and THEN a concurrent unlink lands during the cachePhoto await,
        // which happens after the first recheck but before the final write.
        val personRepository = FakePersonRepository()
        val seeded = personRepository.seed(person("Priya", nativeLookupKey = "key-1", nativeContactId = 1L))
        val resolved = NativeContact(
            lookupKey = "key-1",
            contactId = 1L,
            name = "Priya Updated",
            phoneNumber = null,
            email = null,
            photoUri = null
        )
        val contactsRepository = object : com.jksalcedo.tend.domain.repository.ContactsRepository {
            override suspend fun getImportableContacts(): List<NativeContact> = emptyList()
            override suspend fun resolveContact(lookupKey: String, cachedContactId: Long?): NativeContact? =
                resolved

            override suspend fun cachePhoto(contactId: Long): String? {
                // Simulate a concurrent unlink happening between resolveContact and this call.
                personRepository.updatePerson(seeded.copy(nativeLookupKey = null, nativeContactId = null))
                return null
            }

            override suspend fun createContact(
                name: String,
                phoneNumber: String?,
                email: String?,
                photoUri: String?
            ): NativeContact = error("not used in this test")
        }
        val useCase = RefreshLinkedContactsUseCase(contactsRepository, personRepository)

        useCase()

        val current = personRepository.getPersonById(seeded.id)
        assertEquals(null, current?.nativeLookupKey)
        assertEquals("Priya", current?.name)
    }
}

private open class ContactsRepositoryForRaceTest(
    private val resolved: NativeContact
) : com.jksalcedo.tend.domain.repository.ContactsRepository {
    override suspend fun getImportableContacts(): List<NativeContact> = emptyList()
    override suspend fun resolveContact(lookupKey: String, cachedContactId: Long?): NativeContact? = resolved
    override suspend fun cachePhoto(contactId: Long): String? = null
    override suspend fun createContact(
        name: String,
        phoneNumber: String?,
        email: String?,
        photoUri: String?
    ): NativeContact = error("not used in this test")
}

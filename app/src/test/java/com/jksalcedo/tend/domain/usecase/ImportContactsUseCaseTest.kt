package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.NativeContact
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class ImportContactsUseCaseTest {

    private val contact = NativeContact(
        lookupKey = "key-1",
        contactId = 42L,
        name = "Sam",
        phoneNumber = "555-0100",
        email = "sam@example.com",
        photoUri = "content://contacts/photo/42"
    )

    @Test
    fun `creates a linked person per selected native contact`() = runBlocking {
        val personRepository = FakePersonRepository()
        val useCase = ImportContactsUseCase(personRepository)

        useCase(listOf(contact))

        assertEquals(1, personRepository.insertedPeople.size)
        val person = personRepository.insertedPeople.first()
        assertEquals(contact.name, person.name)
        assertEquals(contact.phoneNumber, person.phoneNumber)
        assertEquals(contact.email, person.email)
        assertEquals(contact.photoUri, person.photoUri)
        assertEquals(contact.lookupKey, person.nativeLookupKey)
        assertEquals(contact.contactId, person.nativeContactId)
    }

    @Test
    fun `relationship fields start at their defaults`() = runBlocking {
        val personRepository = FakePersonRepository()
        val useCase = ImportContactsUseCase(personRepository)

        useCase(listOf(contact), frequencyDays = 14)

        val person = personRepository.insertedPeople.first()
        assertEquals(14, person.frequencyDays)
        assertTrue(person.notes.isEmpty())
        assertTrue(person.events.isEmpty())
        assertTrue(person.socialLinks.isEmpty())
        assertFalse(person.isArchived)
        val expectedNextReminder = person.lastContactedAt + TimeUnit.DAYS.toMillis(14)
        assertEquals(expectedNextReminder, person.nextReminderAt)
    }

    @Test
    fun `creates one person per contact when multiple are selected`() = runBlocking {
        val personRepository = FakePersonRepository()
        val useCase = ImportContactsUseCase(personRepository)
        val secondContact = contact.copy(lookupKey = "key-2", contactId = 43L, name = "Jordan")

        useCase(listOf(contact, secondContact))

        assertEquals(2, personRepository.insertedPeople.size)
    }

    @Test
    fun `does nothing when no contacts are selected`() = runBlocking {
        val personRepository = FakePersonRepository()
        val useCase = ImportContactsUseCase(personRepository)

        useCase(emptyList())

        assertTrue(personRepository.insertedPeople.isEmpty())
    }
}

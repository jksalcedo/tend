package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.NativeContact
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetImportableContactsUseCaseTest {

    @Test
    fun `returns contacts from the repository unchanged`() = runBlocking {
        val contacts = listOf(
            NativeContact(
                lookupKey = "key-1",
                contactId = 1L,
                name = "Sam",
                phoneNumber = "555-0100",
                email = "sam@example.com",
                photoUri = null
            )
        )
        val useCase = GetImportableContactsUseCase(FakeContactsRepository(contacts))

        val result = useCase()

        assertEquals(contacts, result)
    }

    @Test
    fun `returns empty list when repository has nothing importable`() = runBlocking {
        val useCase = GetImportableContactsUseCase(FakeContactsRepository(emptyList()))

        val result = useCase()

        assertEquals(emptyList<NativeContact>(), result)
    }
}

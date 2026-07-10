package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

class ExportDataUseCaseTest {

    @Test
    fun `invoke writes correct JSON to output stream`() = runBlocking {
        // Arrange
        val fakePerson = Person(
            id = 1L,
            name = "Test Person",
            photoUri = null,
            phoneNumber = null,
            email = null,
            events = emptyList(),
            notes = emptyList(),
            socialLinks = emptyList(),
            frequencyDays = 7,
            lastContactedAt = 0L,
            nextReminderAt = 0L,
            isArchived = false
        )
        
        val fakeRepository = object : PersonRepository {
            override fun getAllPeople(): Flow<List<Person>> = flowOf(listOf(fakePerson))
            override fun getArchivedPeople(): Flow<List<Person>> = flowOf(emptyList())
            override suspend fun getPersonById(id: Long): Person? = null
            override suspend fun insertPerson(person: Person) {}
            override suspend fun updatePerson(person: Person) {}
            override suspend fun deletePerson(id: Long) {}
            override suspend fun getAllPeopleList(): List<Person> = listOf(fakePerson)
            override suspend fun insertAll(people: List<Person>) {}
            override suspend fun deleteAllPeople() {}
        }
        
        val exportDataUseCase = ExportDataUseCase(fakeRepository)
        val outputStream = ByteArrayOutputStream()

        // Act
        exportDataUseCase(outputStream)

        // Assert
        val jsonOutput = outputStream.toString("UTF-8")
        assertTrue(jsonOutput.contains("Test Person"))
        assertTrue(jsonOutput.contains("\"id\":1"))
    }
}

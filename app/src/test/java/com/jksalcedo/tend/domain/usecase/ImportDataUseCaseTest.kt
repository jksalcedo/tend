package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class ImportDataUseCaseTest {

    @Test
    fun `invoke deletes all and inserts parsed JSON data`() = runBlocking {
        // Arrange
        val jsonInput = """
            [
              {
                "id": 1,
                "name": "Test Person",
                "frequencyDays": 7,
                "lastContactedAt": 0,
                "nextReminderAt": 0,
                "isArchived": false,
                "events": [],
                "notes": [],
                "socialLinks": []
              }
            ]
        """.trimIndent()
        val inputStream = ByteArrayInputStream(jsonInput.toByteArray(Charsets.UTF_8))
        
        var deletedAllCalled = false
        var insertedPeople: List<Person>? = null
        
        val fakeRepository = object : PersonRepository {
            override fun getAllPeople(): Flow<List<Person>> = flowOf(emptyList())
            override fun getArchivedPeople(): Flow<List<Person>> = flowOf(emptyList())
            override suspend fun getPersonById(id: Long): Person? = null
            override suspend fun insertPerson(person: Person) {}
            override suspend fun updatePerson(person: Person) {}
            override suspend fun deletePerson(id: Long) {}
            override suspend fun getAllPeopleList(): List<Person> = emptyList()
            override suspend fun insertAll(people: List<Person>) {
                insertedPeople = people
            }
            override suspend fun deleteAllPeople() {
                deletedAllCalled = true
            }
        }
        
        val importDataUseCase = ImportDataUseCase(fakeRepository)

        // Act
        importDataUseCase(inputStream)

        // Assert
        assertTrue("deleteAllPeople should be called", deletedAllCalled)
        assertEquals("One person should be inserted", 1, insertedPeople?.size)
        assertEquals("Test Person", insertedPeople?.first()?.name)
    }
}

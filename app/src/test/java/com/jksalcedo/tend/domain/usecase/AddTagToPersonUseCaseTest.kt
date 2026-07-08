package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddTagToPersonUseCaseTest {

    private fun person(name: String) = Person(
        name = name,
        photoUri = null,
        frequencyDays = 14,
        lastContactedAt = 0L,
        nextReminderAt = 0L
    )

    @Test
    fun `adds a new tag to the person and to the pool`() = runBlocking {
        val personRepository = FakePersonRepository()
        val tagRepository = FakeTagRepository()
        val seeded = personRepository.seed(person("Priya"))
        val useCase = AddTagToPersonUseCase(personRepository, tagRepository)

        useCase(seeded.id, "Book Club")

        assertEquals(listOf("Book Club"), personRepository.getPersonById(seeded.id)?.tags)
        assertTrue("Book Club" in tagRepository.observeAllTags().first())
    }

    @Test
    fun `does not duplicate a tag the person already has`() = runBlocking {
        val personRepository = FakePersonRepository()
        val tagRepository = FakeTagRepository()
        val seeded = personRepository.seed(person("Priya").copy(tags = listOf("Family")))
        val useCase = AddTagToPersonUseCase(personRepository, tagRepository)

        useCase(seeded.id, "Family")

        assertEquals(listOf("Family"), personRepository.getPersonById(seeded.id)?.tags)
    }

    @Test
    fun `blank tag is ignored`() = runBlocking {
        val personRepository = FakePersonRepository()
        val tagRepository = FakeTagRepository()
        val seeded = personRepository.seed(person("Priya"))
        val useCase = AddTagToPersonUseCase(personRepository, tagRepository)

        useCase(seeded.id, "   ")

        assertEquals(emptyList<String>(), personRepository.getPersonById(seeded.id)?.tags)
    }
}

package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoveTagFromPersonUseCaseTest {

    private fun person(name: String) = Person(
        name = name,
        photoUri = null,
        frequencyDays = 14,
        lastContactedAt = 0L,
        nextReminderAt = 0L
    )

    @Test
    fun `removes the tag from the person only, not from the pool`() = runBlocking {
        val personRepository = FakePersonRepository()
        val tagRepository = FakeTagRepository()
        tagRepository.seed("Book Club")
        val seeded = personRepository.seed(person("Priya").copy(tags = listOf("Book Club")))
        val useCase = RemoveTagFromPersonUseCase(personRepository)

        useCase(seeded.id, "Book Club")

        assertEquals(emptyList<String>(), personRepository.getPersonById(seeded.id)?.tags)
        assertTrue("Book Club" in tagRepository.observeAllTags().first())
    }

    @Test
    fun `does not affect another person with the same tag`() = runBlocking {
        val personRepository = FakePersonRepository()
        val priya = personRepository.seed(person("Priya").copy(tags = listOf("Family")))
        val marco = personRepository.seed(person("Marco").copy(tags = listOf("Family")))
        val useCase = RemoveTagFromPersonUseCase(personRepository)

        useCase(priya.id, "Family")

        assertEquals(emptyList<String>(), personRepository.getPersonById(priya.id)?.tags)
        assertEquals(listOf("Family"), personRepository.getPersonById(marco.id)?.tags)
    }
}

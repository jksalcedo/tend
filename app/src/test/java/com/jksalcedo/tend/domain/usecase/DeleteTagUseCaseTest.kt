package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DeleteTagUseCaseTest {

    private fun person(name: String) = Person(
        name = name,
        photoUri = null,
        frequencyDays = 14,
        lastContactedAt = 0L,
        nextReminderAt = 0L
    )

    @Test
    fun `removes the tag from the pool and from every person wearing it`() = runBlocking {
        val personRepository = FakePersonRepository()
        val tagRepository = FakeTagRepository()
        tagRepository.seed("Family", "Friend")
        val priya = personRepository.seed(person("Priya").copy(tags = listOf("Family", "Neighbor")))
        val marco = personRepository.seed(person("Marco").copy(tags = listOf("Family")))
        val useCase = DeleteTagUseCase(tagRepository, personRepository)

        useCase("Family")

        assertFalse("Family" in tagRepository.observeAllTags().first())
        assertEquals(listOf("Neighbor"), personRepository.getPersonById(priya.id)?.tags)
        assertEquals(emptyList<String>(), personRepository.getPersonById(marco.id)?.tags)
    }

    @Test
    fun `deleting an unused tag still removes it from the pool`() = runBlocking {
        val personRepository = FakePersonRepository()
        val tagRepository = FakeTagRepository()
        tagRepository.seed("Book Club")
        val useCase = DeleteTagUseCase(tagRepository, personRepository)

        useCase("Book Club")

        assertFalse("Book Club" in tagRepository.observeAllTags().first())
    }
}

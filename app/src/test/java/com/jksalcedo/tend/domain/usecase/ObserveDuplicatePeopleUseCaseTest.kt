package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveDuplicatePeopleUseCaseTest {

    private fun person(name: String, nativeLookupKey: String?) = Person(
        name = name,
        photoUri = null,
        frequencyDays = 14,
        lastContactedAt = 0L,
        nextReminderAt = 0L,
        nativeLookupKey = nativeLookupKey
    )

    @Test
    fun `returns other people sharing the same lookup key`() = runBlocking {
        val personRepository = FakePersonRepository()
        val a = personRepository.seed(person("A", "shared-key"))
        val b = personRepository.seed(person("B", "shared-key"))
        val c = personRepository.seed(person("C", "shared-key"))
        val useCase = ObserveDuplicatePeopleUseCase(personRepository)

        val duplicatesOfA = useCase("shared-key", a.id).first()

        assertEquals(setOf(b.id, c.id), duplicatesOfA.map { it.id }.toSet())
    }

    @Test
    fun `returns empty when no one else shares the lookup key`() = runBlocking {
        val personRepository = FakePersonRepository()
        val a = personRepository.seed(person("A", "unique-key"))
        val useCase = ObserveDuplicatePeopleUseCase(personRepository)

        val duplicatesOfA = useCase("unique-key", a.id).first()

        assertTrue(duplicatesOfA.isEmpty())
    }
}

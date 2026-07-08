package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MaybeShowContactImportPromptUseCaseTest {

    private fun person(name: String) = Person(
        name = name,
        photoUri = null,
        frequencyDays = 14,
        lastContactedAt = 0L,
        nextReminderAt = 0L
    )

    @Test
    fun `shows the prompt when unresolved and no people exist`() = runBlocking {
        val onboardingRepository = FakeOnboardingRepository(resolved = false)
        val personRepository = FakePersonRepository()
        val useCase = MaybeShowContactImportPromptUseCase(onboardingRepository, personRepository)

        val shouldShow = useCase()

        assertTrue(shouldShow)
        assertEquals(0, onboardingRepository.markResolvedCallCount)
    }

    @Test
    fun `does not show and silently resolves when people already exist`() = runBlocking {
        val onboardingRepository = FakeOnboardingRepository(resolved = false)
        val personRepository = FakePersonRepository()
        personRepository.seed(person("Marco"))
        val useCase = MaybeShowContactImportPromptUseCase(onboardingRepository, personRepository)

        val shouldShow = useCase()

        assertFalse(shouldShow)
        assertEquals(1, onboardingRepository.markResolvedCallCount)
        assertTrue(onboardingRepository.isContactImportPromptResolved())
    }

    @Test
    fun `never shows again once already resolved, regardless of people count`() = runBlocking {
        val onboardingRepository = FakeOnboardingRepository(resolved = true)
        val personRepository = FakePersonRepository()
        val useCase = MaybeShowContactImportPromptUseCase(onboardingRepository, personRepository)

        val shouldShow = useCase()

        assertFalse(shouldShow)
        // Already resolved — shouldn't redundantly call markContactImportPromptResolved again.
        assertEquals(0, onboardingRepository.markResolvedCallCount)
    }
}

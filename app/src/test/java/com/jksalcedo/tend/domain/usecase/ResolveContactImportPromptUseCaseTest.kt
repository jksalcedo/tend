package com.jksalcedo.tend.domain.usecase

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveContactImportPromptUseCaseTest {

    @Test
    fun `marks the prompt resolved`() = runBlocking {
        val onboardingRepository = FakeOnboardingRepository(resolved = false)
        val useCase = ResolveContactImportPromptUseCase(onboardingRepository)

        useCase()

        assertTrue(onboardingRepository.isContactImportPromptResolved())
    }
}

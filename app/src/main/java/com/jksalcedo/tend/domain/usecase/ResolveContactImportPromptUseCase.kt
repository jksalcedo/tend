package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.repository.OnboardingRepository

// Marks the first-run import prompt resolved — called whether the user picked "Yes,"
// "No," or dismissed it, since all three are treated as final per the spec.
class ResolveContactImportPromptUseCase(
    private val onboardingRepository: OnboardingRepository
) {
    suspend operator fun invoke() {
        onboardingRepository.markContactImportPromptResolved()
    }
}

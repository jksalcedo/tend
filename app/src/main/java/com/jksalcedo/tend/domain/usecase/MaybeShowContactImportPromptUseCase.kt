package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.repository.OnboardingRepository
import com.jksalcedo.tend.domain.repository.PersonRepository
import kotlinx.coroutines.flow.first

// Whether the first-run "import your contacts?" prompt should be shown right now. Once
// resolved (shown-and-answered, or silently skipped because people already existed), it
// never shows again — resolution is permanent, independent of later deleting everyone.
class MaybeShowContactImportPromptUseCase(
    private val onboardingRepository: OnboardingRepository,
    private val personRepository: PersonRepository
) {
    suspend operator fun invoke(): Boolean {
        if (onboardingRepository.isContactImportPromptResolved()) return false

        val hasAnyPeople = personRepository.getAllPeople().first().isNotEmpty()
        if (hasAnyPeople) {
            onboardingRepository.markContactImportPromptResolved()
            return false
        }

        return true
    }
}

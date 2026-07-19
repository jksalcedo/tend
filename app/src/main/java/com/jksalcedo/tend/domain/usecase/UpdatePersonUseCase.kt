package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Note
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.model.PersonEvent
import com.jksalcedo.tend.domain.model.SocialLink
import com.jksalcedo.tend.domain.repository.PersonRepository

class UpdatePersonUseCase(
    private val repository: PersonRepository
) {
    suspend operator fun invoke(
        personId: Long,
        name: String,
        frequencyDays: Int,
        phoneNumber: String?,
        email: String?,
        socialLinks: List<SocialLink>,
        events: List<PersonEvent>,
        reminderWindowDays: Int
    ) {
        val existing = repository.getPersonById(personId) ?: return
        val finalDays = if (reminderWindowDays > 0) {
            val minDays = maxOf(1, frequencyDays - reminderWindowDays)
            val maxDays = frequencyDays + reminderWindowDays
            kotlin.random.Random.nextInt(minDays, maxDays + 1)
        } else {
            frequencyDays
        }
        val nextReminder = existing.lastContactedAt + (finalDays * 24 * 60 * 60 * 1000L)
        repository.updatePerson(
            existing.copy(
                name = name,
                frequencyDays = frequencyDays,
                nextReminderAt = nextReminder,
                phoneNumber = phoneNumber,
                email = email,
                socialLinks = socialLinks,
                events = events,
                reminderWindowDays = reminderWindowDays
            )
        )
    }
}

package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.PersonRepository
class AddPersonUseCase(
    private val repository: PersonRepository
) {
    suspend operator fun invoke(
        name: String,
        frequencyDays: Int,
        initialNote: String = "",
        photoUri: String? = null,
        phoneNumber: String? = null,
        email: String? = null,
        socialLinks: List<com.jksalcedo.tend.domain.model.SocialLink> = emptyList(),
        events: List<com.jksalcedo.tend.domain.model.PersonEvent> = emptyList(),
        reminderWindowDays: Int = 0
    ) {
        val now = System.currentTimeMillis()
        val finalDays = if (reminderWindowDays > 0) {
            val minDays = maxOf(1, frequencyDays - reminderWindowDays)
            val maxDays = frequencyDays + reminderWindowDays
            kotlin.random.Random.nextInt(minDays, maxDays + 1)
        } else {
            frequencyDays
        }
        val nextReminder = now + (finalDays * 24 * 60 * 60 * 1000L)
        
        val notesList = if (initialNote.isNotBlank()) {
            listOf(com.jksalcedo.tend.domain.model.Note(content = initialNote))
        } else {
            emptyList()
        }

        val person = Person(
            name = name,
            photoUri = photoUri,
            phoneNumber = phoneNumber,
            email = email,
            notes = notesList,
            socialLinks = socialLinks,
            events = events,
            frequencyDays = frequencyDays,
            lastContactedAt = now,
            nextReminderAt = nextReminder,
            reminderWindowDays = reminderWindowDays
        )
        repository.insertPerson(person)
    }
}

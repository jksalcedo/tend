package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.PersonRepository
import javax.inject.Inject

class AddPersonUseCase @Inject constructor(
    private val repository: PersonRepository
) {
    suspend operator fun invoke(
        name: String,
        cadenceDays: Int,
        initialNote: String = "",
        photoUri: String? = null,
        phoneNumber: String? = null,
        email: String? = null,
        socialLinks: List<com.jksalcedo.tend.domain.model.SocialLink> = emptyList(),
        events: List<com.jksalcedo.tend.domain.model.PersonEvent> = emptyList()
    ) {
        val now = System.currentTimeMillis()
        val nextReminder = now + (cadenceDays * 24 * 60 * 60 * 1000L)
        
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
            cadenceDays = cadenceDays,
            lastContactedAt = now,
            nextReminderAt = nextReminder
        )
        repository.insertPerson(person)
    }
}

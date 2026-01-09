package com.jksalcedo.crontact.domain.usecase

import com.jksalcedo.crontact.domain.model.Person
import com.jksalcedo.crontact.domain.repository.PersonRepository
import javax.inject.Inject

class AddPersonUseCase @Inject constructor(
    private val repository: PersonRepository
) {
    suspend operator fun invoke(
        name: String,
        cadenceDays: Int,
        notes: String = "",
        photoUri: String? = null
    ) {
        val now = System.currentTimeMillis()
        // Default next reminder is now + cadence
        // Or should it be now? The prompt says "The Mechanic: A 'Loop.' You contact someone, the timer resets...".
        // But initially, if I add someone, when are they due?
        // Let's assume they are due immediately or we can set it to now + cadence.
        // Prompt says: "Entry: ... set a specific cadence".
        // Let's assume we just met them or want to start tracking.
        // If I add them, maybe I just contacted them? Or maybe I want to contact them in X days.
        // Let's set lastContactedAt to NOW, and nextReminderAt to NOW + cadenceDays * 24hr.
        
        val nextReminder = now + (cadenceDays * 24 * 60 * 60 * 1000L)
        
        val person = Person(
            name = name,
            photoUri = photoUri,
            notes = notes,
            cadenceDays = cadenceDays,
            lastContactedAt = now,
            nextReminderAt = nextReminder
        )
        repository.insertPerson(person)
    }
}

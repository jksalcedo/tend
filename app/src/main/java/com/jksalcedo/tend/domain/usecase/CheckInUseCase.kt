package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.InteractionType
import com.jksalcedo.tend.domain.repository.CheckInHistoryRepository
import com.jksalcedo.tend.domain.repository.PersonRepository
import kotlin.random.Random

class CheckInUseCase(
    private val repository: PersonRepository,
    private val historyRepository: CheckInHistoryRepository
) {
    suspend operator fun invoke(
        personId: Long,
        interactionType: InteractionType? = null,
        note: String? = null
    ) {
        val person = repository.getPersonById(personId) ?: return
        val now = System.currentTimeMillis()
        val floatDays = person.reminderWindowDays
        val finalDays = if (floatDays > 0) {
            val minDays = maxOf(1, person.frequencyDays - floatDays)
            val maxDays = person.frequencyDays + floatDays
            Random.nextInt(minDays, maxDays + 1)
        } else {
            person.frequencyDays
        }
        val next = now + (finalDays * 24 * 60 * 60 * 1000L)
        
        repository.updatePerson(
            person.copy(lastContactedAt = now, nextReminderAt = next)
        )
        
        historyRepository.recordCheckIn(
            personId = personId,
            timestamp = now,
            interactionType = interactionType,
            note = note
        )
    }
}

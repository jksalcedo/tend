package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.repository.PersonRepository
import java.util.concurrent.TimeUnit

class CheckInUseCase(
    private val repository: PersonRepository
) {
    suspend operator fun invoke(personId: Long) {
        val person = repository.getPersonById(personId) ?: return
        val now = System.currentTimeMillis()
        val next = now + TimeUnit.DAYS.toMillis(person.frequencyDays.toLong())
        repository.updatePerson(
            person.copy(lastContactedAt = now, nextReminderAt = next)
        )
    }
}

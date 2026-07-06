package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.repository.PersonRepository

class UnarchivePersonUseCase(
    private val repository: PersonRepository
) {
    suspend operator fun invoke(id: Long) {
        val person = repository.getPersonById(id) ?: return
        repository.updatePerson(person.copy(isArchived = false))
    }
}

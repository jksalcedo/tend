package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.repository.PersonRepository

class DeletePersonUseCase(
    private val repository: PersonRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deletePerson(id)
    }
}

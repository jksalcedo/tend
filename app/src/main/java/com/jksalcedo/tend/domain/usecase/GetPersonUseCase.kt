package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.PersonRepository
class GetPersonUseCase(
    private val repository: PersonRepository
) {
    suspend operator fun invoke(id: Long): Person? {
        return repository.getPersonById(id)
    }
}

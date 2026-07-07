package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow

class ObservePersonUseCase(
    private val repository: PersonRepository
) {
    operator fun invoke(id: Long): Flow<Person?> {
        return repository.observePersonById(id)
    }
}

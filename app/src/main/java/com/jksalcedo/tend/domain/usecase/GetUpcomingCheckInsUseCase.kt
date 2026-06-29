package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
class GetUpcomingCheckInsUseCase(
    private val repository: PersonRepository
) {
    operator fun invoke(): Flow<List<Person>> {
        return repository.getAllPeople()
    }
}

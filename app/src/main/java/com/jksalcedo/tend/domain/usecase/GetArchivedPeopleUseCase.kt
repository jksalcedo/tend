package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow

class GetArchivedPeopleUseCase(
    private val repository: PersonRepository
) {
    operator fun invoke(): Flow<List<Person>> {
        return repository.getArchivedPeople()
    }
}

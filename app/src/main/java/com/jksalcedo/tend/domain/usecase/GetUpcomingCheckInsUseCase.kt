package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUpcomingCheckInsUseCase @Inject constructor(
    private val repository: PersonRepository
) {
    operator fun invoke(): Flow<List<Person>> {
        return repository.getAllPeople()
    }
}

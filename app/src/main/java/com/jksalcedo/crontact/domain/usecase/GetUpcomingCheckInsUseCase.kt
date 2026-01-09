package com.jksalcedo.crontact.domain.usecase

import com.jksalcedo.crontact.domain.model.Person
import com.jksalcedo.crontact.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUpcomingCheckInsUseCase @Inject constructor(
    private val repository: PersonRepository
) {
    operator fun invoke(): Flow<List<Person>> {
        return repository.getAllPeople()
    }
}

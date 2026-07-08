package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow

// Other Tend people currently sharing this person's native lookup key — a live,
// derived view rather than a stored flag, so it can never go stale and naturally
// covers any group size (not just pairs).
class ObserveDuplicatePeopleUseCase(
    private val repository: PersonRepository
) {
    operator fun invoke(lookupKey: String, excludeId: Long): Flow<List<Person>> {
        return repository.observeDuplicatesOf(lookupKey, excludeId)
    }
}

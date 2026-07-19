package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.repository.PersonRepository

// Removes a tag from one person only — never touches the pool, even if this was the last
// person wearing it. Deleting a tag from the pool is a separate, explicit action; see
// DeleteTagUseCase.
class RemoveTagFromPersonUseCase(
    private val personRepository: PersonRepository
) {
    suspend operator fun invoke(personId: Long, tag: String) {
        val person = personRepository.getPersonById(personId) ?: return
        personRepository.updatePerson(person.copy(tags = person.tags - tag))
    }
}

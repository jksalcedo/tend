package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.repository.PersonRepository
import com.jksalcedo.tend.domain.repository.TagRepository

// The only way a tag leaves the pool — cascades to every person wearing it, including
// archived ones, so no one is left with a tag that's no longer selectable for anyone else.
class DeleteTagUseCase(
    private val tagRepository: TagRepository,
    private val personRepository: PersonRepository
) {
    suspend operator fun invoke(tag: String) {
        tagRepository.deleteTag(tag)
        personRepository.getEveryPerson()
            .filter { tag in it.tags }
            .forEach { person ->
                personRepository.updatePerson(person.copy(tags = person.tags - tag))
            }
    }
}

package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.repository.PersonRepository
import com.jksalcedo.tend.domain.repository.TagRepository

class AddTagToPersonUseCase(
    private val personRepository: PersonRepository,
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(personId: Long, tag: String) {
        val trimmed = tag.trim()
        if (trimmed.isBlank()) return
        // Joins the pool even if this exact tag already exists there (no-op via IGNORE) —
        // needed so a brand-new, never-seen tag becomes a suggestion for other people too.
        tagRepository.ensureTagExists(trimmed)
        val person = personRepository.getPersonById(personId) ?: return
        if (trimmed in person.tags) return
        personRepository.updatePerson(person.copy(tags = person.tags + trimmed))
    }
}

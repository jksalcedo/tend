package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.repository.PersonRepository

class DeleteNoteUseCase(
    private val repository: PersonRepository
) {
    suspend operator fun invoke(personId: Long, noteId: String) {
        val person = repository.getPersonById(personId) ?: return
        val updatedNotes = person.notes.filterNot { it.id == noteId }
        repository.updatePerson(person.copy(notes = updatedNotes))
    }
}

package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.repository.PersonRepository

class UpdateNoteUseCase(
    private val repository: PersonRepository
) {
    suspend operator fun invoke(personId: Long, noteId: String, newContent: String) {
        if (newContent.isBlank()) return
        val person = repository.getPersonById(personId) ?: return
        val updatedNotes = person.notes.map { note ->
            if (note.id == noteId) {
                note.copy(content = newContent)
            } else {
                note
            }
        }
        repository.updatePerson(person.copy(notes = updatedNotes))
    }
}

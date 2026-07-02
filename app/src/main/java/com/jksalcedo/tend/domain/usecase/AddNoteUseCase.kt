package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.Note
import com.jksalcedo.tend.domain.repository.PersonRepository

class AddNoteUseCase(
    private val repository: PersonRepository
) {
    suspend operator fun invoke(personId: Long, content: String) {
        val person = repository.getPersonById(personId) ?: return
        val note = Note(content = content)
        repository.updatePerson(person.copy(notes = person.notes + note))
    }
}

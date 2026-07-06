package com.jksalcedo.tend.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.usecase.AddNoteUseCase
import com.jksalcedo.tend.domain.usecase.ArchivePersonUseCase
import com.jksalcedo.tend.domain.usecase.CheckInUseCase
import com.jksalcedo.tend.domain.usecase.DeletePersonUseCase
import com.jksalcedo.tend.domain.usecase.GetPersonUseCase
import com.jksalcedo.tend.domain.usecase.UnarchivePersonUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonDetailViewModel(
    private val getPersonUseCase: GetPersonUseCase,
    private val checkInUseCase: CheckInUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val archivePersonUseCase: ArchivePersonUseCase,
    private val deletePersonUseCase: DeletePersonUseCase,
    private val unarchivePersonUseCase: UnarchivePersonUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId: Long? = savedStateHandle["personId"]

    private val _person = MutableStateFlow<Person?>(null)
    val person: StateFlow<Person?> = _person.asStateFlow()

    init {
        if (personId != null) loadPerson(personId)
    }

    private fun loadPerson(id: Long) {
        viewModelScope.launch {
            _person.value = getPersonUseCase(id)
        }
    }

    fun checkIn() {
        val id = personId ?: return
        viewModelScope.launch {
            checkInUseCase(id)
            _person.value = getPersonUseCase(id)
        }
    }

    fun addNote(content: String) {
        val id = personId ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            addNoteUseCase(id, content)
            _person.value = getPersonUseCase(id)
        }
    }

    fun archive(onComplete: () -> Unit) {
        val id = personId ?: return
        viewModelScope.launch {
            archivePersonUseCase(id)
            onComplete()
        }
    }

    fun unarchive(onComplete: () -> Unit) {
        val id = personId ?: return
        viewModelScope.launch {
            unarchivePersonUseCase(id)
            onComplete()
        }
    }

    fun delete(onComplete: () -> Unit) {
        val id = personId ?: return
        viewModelScope.launch {
            deletePersonUseCase(id)
            onComplete()
        }
    }
}

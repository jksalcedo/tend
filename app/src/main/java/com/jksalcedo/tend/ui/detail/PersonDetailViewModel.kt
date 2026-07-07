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
import com.jksalcedo.tend.domain.usecase.ObservePersonUseCase
import com.jksalcedo.tend.domain.usecase.UnarchivePersonUseCase
import com.jksalcedo.tend.domain.usecase.UnlinkPersonUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PersonDetailViewModel(
    observePersonUseCase: ObservePersonUseCase,
    private val getPersonUseCase: GetPersonUseCase,
    private val checkInUseCase: CheckInUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val archivePersonUseCase: ArchivePersonUseCase,
    private val deletePersonUseCase: DeletePersonUseCase,
    private val unarchivePersonUseCase: UnarchivePersonUseCase,
    private val unlinkPersonUseCase: UnlinkPersonUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId: Long? = savedStateHandle["personId"]

    // A live Room query rather than a one-shot fetch: this is what actually makes
    // "foreground refresh picks up device contact changes" visible on screen — the
    // refresh use case writes to the DB on its own schedule, and this flow reflects
    // that the moment it happens, with no manual reload/race to get right.
    val person: StateFlow<Person?> = (personId?.let { observePersonUseCase(it) } ?: flowOf(null))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _duplicatePerson = MutableStateFlow<Person?>(null)
    val duplicatePerson: StateFlow<Person?> = _duplicatePerson.asStateFlow()

    init {
        viewModelScope.launch {
            person.collect { p ->
                _duplicatePerson.value = p?.duplicateOfPersonId?.let { getPersonUseCase(it) }
            }
        }
    }

    fun checkIn() {
        val id = personId ?: return
        viewModelScope.launch { checkInUseCase(id) }
    }

    fun addNote(content: String) {
        val id = personId ?: return
        if (content.isBlank()) return
        viewModelScope.launch { addNoteUseCase(id, content) }
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

    fun unlink() {
        val id = personId ?: return
        viewModelScope.launch { unlinkPersonUseCase(id) }
    }
}

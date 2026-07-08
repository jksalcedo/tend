package com.jksalcedo.tend.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.usecase.AddNoteUseCase
import com.jksalcedo.tend.domain.usecase.ArchivePersonUseCase
import com.jksalcedo.tend.domain.usecase.CheckInUseCase
import com.jksalcedo.tend.domain.usecase.DeleteNoteUseCase
import com.jksalcedo.tend.domain.usecase.DeletePersonUseCase
import com.jksalcedo.tend.domain.usecase.ObserveDuplicatePeopleUseCase
import com.jksalcedo.tend.domain.usecase.ObservePersonUseCase
import com.jksalcedo.tend.domain.usecase.SyncToDeviceUseCase
import com.jksalcedo.tend.domain.usecase.UnarchivePersonUseCase
import com.jksalcedo.tend.domain.usecase.UnlinkPersonUseCase
import com.jksalcedo.tend.domain.usecase.UpdateNoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PersonDetailViewModel(
    observePersonUseCase: ObservePersonUseCase,
    observeDuplicatePeopleUseCase: ObserveDuplicatePeopleUseCase,
    private val checkInUseCase: CheckInUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val archivePersonUseCase: ArchivePersonUseCase,
    private val deletePersonUseCase: DeletePersonUseCase,
    private val unarchivePersonUseCase: UnarchivePersonUseCase,
    private val unlinkPersonUseCase: UnlinkPersonUseCase,
    private val syncToDeviceUseCase: SyncToDeviceUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId: Long? = savedStateHandle["personId"]

    // A live Room query rather than a one-shot fetch: this is what actually makes
    // "foreground refresh picks up device contact changes" visible on screen — the
    // refresh use case writes to the DB on its own schedule, and this flow reflects
    // that the moment it happens, with no manual reload/race to get right.
    val person: StateFlow<Person?> = (personId?.let { observePersonUseCase(it) } ?: flowOf(null))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Other people currently sharing this person's native lookup key. Derived live from
    // the current person's own lookup key (re-subscribing whenever it changes) rather than
    // a one-shot fetch off a stored pointer, so it can never go stale and naturally covers
    // groups of any size.
    val duplicates: StateFlow<List<Person>> = person
        .flatMapLatest { p ->
            val lookupKey = p?.nativeLookupKey
            if (lookupKey != null) observeDuplicatePeopleUseCase(lookupKey, p.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncFailed = MutableStateFlow(false)
    val syncFailed: StateFlow<Boolean> = _syncFailed.asStateFlow()

    fun consumeSyncFailed() {
        _syncFailed.value = false
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

    fun syncToDevice() {
        val id = personId ?: return
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                syncToDeviceUseCase(id)
            } catch (e: Exception) {
                _syncFailed.value = true
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun deleteNote(noteId: String) {
        val id = personId ?: return
        viewModelScope.launch { deleteNoteUseCase(id, noteId) }
    }

    fun updateNote(noteId: String, newContent: String) {
        val id = personId ?: return
        if (newContent.isBlank()) return
        viewModelScope.launch { updateNoteUseCase(id, noteId, newContent) }
    }
}

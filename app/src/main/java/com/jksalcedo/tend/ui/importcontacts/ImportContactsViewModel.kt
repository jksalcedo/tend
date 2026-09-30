package com.jksalcedo.tend.ui.importcontacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.tend.domain.model.NativeContact
import com.jksalcedo.tend.domain.usecase.GetImportableContactsUseCase
import com.jksalcedo.tend.domain.usecase.ImportContactsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImportContactsViewModel(
    private val getImportableContactsUseCase: GetImportableContactsUseCase,
    private val importContactsUseCase: ImportContactsUseCase
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<NativeContact>>(emptyList())
    val contacts: StateFlow<List<NativeContact>> = _contacts.asStateFlow()

    private val _selectedLookupKeys = MutableStateFlow<Set<String>>(emptySet())
    val selectedLookupKeys: StateFlow<Set<String>> = _selectedLookupKeys.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            _contacts.value = getImportableContactsUseCase()
            _isLoading.value = false
        }
    }

    fun toggleSelection(lookupKey: String) {
        _selectedLookupKeys.value = if (lookupKey in _selectedLookupKeys.value) {
            _selectedLookupKeys.value - lookupKey
        } else {
            _selectedLookupKeys.value + lookupKey
        }
    }

    fun selectAll() {
        _selectedLookupKeys.value = _contacts.value.map { it.lookupKey }.toSet()
    }

    fun selectNone() {
        _selectedLookupKeys.value = emptySet()
    }

    fun confirmImport(onComplete: () -> Unit) {
        val selected = _contacts.value.filter { it.lookupKey in _selectedLookupKeys.value }
        if (selected.isEmpty()) {
            onComplete()
            return
        }
        viewModelScope.launch {
            importContactsUseCase(selected)
            onComplete()
        }
    }
}

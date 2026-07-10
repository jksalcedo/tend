package com.jksalcedo.tend.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.usecase.GetUpcomingCheckInsUseCase
import com.jksalcedo.tend.domain.usecase.MaybeShowContactImportPromptUseCase
import com.jksalcedo.tend.domain.usecase.ObserveAllTagsUseCase
import com.jksalcedo.tend.domain.usecase.ResolveContactImportPromptUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.jksalcedo.tend.domain.usecase.ExportDataUseCase
import com.jksalcedo.tend.domain.usecase.ImportDataUseCase
import kotlinx.coroutines.launch

class HomeViewModel(
    getUpcomingCheckInsUseCase: GetUpcomingCheckInsUseCase,
    observeAllTagsUseCase: ObserveAllTagsUseCase,
    private val maybeShowContactImportPromptUseCase: MaybeShowContactImportPromptUseCase,
    private val resolveContactImportPromptUseCase: ResolveContactImportPromptUseCase
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    val allTags: StateFlow<List<String>> = observeAllTagsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val people: StateFlow<List<Person>> = combine(
        getUpcomingCheckInsUseCase(),
        _searchQuery,
        _selectedTag
    ) { peopleList, query, tag ->
        peopleList
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .filter { tag == null || tag in it.tags }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _showImportPrompt = MutableStateFlow(false)
    val showImportPrompt: StateFlow<Boolean> = _showImportPrompt.asStateFlow()

    init {
        viewModelScope.launch {
            _showImportPrompt.value = maybeShowContactImportPromptUseCase()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedTag(tag: String?) {
        _selectedTag.value = tag
    }

    // Called whether the user tapped Yes, No, or dismissed the prompt — all three resolve
    // it permanently per the spec. Navigating to Import Contacts on Yes is the caller's job.
    fun onImportPromptResolved() {
        _showImportPrompt.value = false
        viewModelScope.launch { resolveContactImportPromptUseCase() }
    fun exportData(outputStream: java.io.OutputStream, onComplete: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                exportDataUseCase(outputStream)
                onComplete()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun importData(inputStream: java.io.InputStream, onComplete: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                importDataUseCase(inputStream)
                onComplete()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}

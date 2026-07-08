package com.jksalcedo.tend.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.usecase.GetUpcomingCheckInsUseCase
import com.jksalcedo.tend.domain.usecase.MaybeShowContactImportPromptUseCase
import com.jksalcedo.tend.domain.usecase.ResolveContactImportPromptUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    getUpcomingCheckInsUseCase: GetUpcomingCheckInsUseCase,
    private val maybeShowContactImportPromptUseCase: MaybeShowContactImportPromptUseCase,
    private val resolveContactImportPromptUseCase: ResolveContactImportPromptUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val people: StateFlow<List<Person>> = combine(
        getUpcomingCheckInsUseCase(),
        _searchQuery
    ) { peopleList, query ->
        if (query.isBlank()) {
            peopleList
        } else {
            peopleList.filter { it.name.contains(query, ignoreCase = true) }
        }
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

    // Called whether the user tapped Yes, No, or dismissed the prompt — all three resolve
    // it permanently per the spec. Navigating to Import Contacts on Yes is the caller's job.
    fun onImportPromptResolved() {
        _showImportPrompt.value = false
        viewModelScope.launch { resolveContactImportPromptUseCase() }
    }
}

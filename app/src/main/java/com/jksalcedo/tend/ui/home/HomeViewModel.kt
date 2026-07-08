package com.jksalcedo.tend.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.usecase.GetUpcomingCheckInsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    getUpcomingCheckInsUseCase: GetUpcomingCheckInsUseCase
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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

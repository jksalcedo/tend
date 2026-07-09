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

import com.jksalcedo.tend.domain.usecase.ExportDataUseCase
import com.jksalcedo.tend.domain.usecase.ImportDataUseCase
import kotlinx.coroutines.launch

class HomeViewModel(
    getUpcomingCheckInsUseCase: GetUpcomingCheckInsUseCase,
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase
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

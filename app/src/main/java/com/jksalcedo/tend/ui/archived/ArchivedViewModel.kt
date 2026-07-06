package com.jksalcedo.tend.ui.archived

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.usecase.GetArchivedPeopleUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ArchivedViewModel(
    getArchivedPeopleUseCase: GetArchivedPeopleUseCase
) : ViewModel() {

    val people: StateFlow<List<Person>> = getArchivedPeopleUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

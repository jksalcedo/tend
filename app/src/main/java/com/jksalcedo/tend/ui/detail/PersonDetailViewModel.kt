package com.jksalcedo.tend.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.usecase.CheckInUseCase
import com.jksalcedo.tend.domain.usecase.GetPersonUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonDetailViewModel(
    private val getPersonUseCase: GetPersonUseCase,
    private val checkInUseCase: CheckInUseCase,
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
}

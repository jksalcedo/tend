package com.jksalcedo.tend.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.usecase.GetPersonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val getPersonUseCase: GetPersonUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _person = MutableStateFlow<Person?>(null)
    val person: StateFlow<Person?> = _person.asStateFlow()

    init {
        val personId = savedStateHandle.get<Long>("personId")
        if (personId != null) {
            loadPerson(personId)
        }
    }

    private fun loadPerson(id: Long) {
        viewModelScope.launch {
            _person.value = getPersonUseCase(id)
        }
    }
}

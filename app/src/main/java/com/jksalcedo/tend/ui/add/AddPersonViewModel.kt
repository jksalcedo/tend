package com.jksalcedo.tend.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.model.PersonEvent
import com.jksalcedo.tend.domain.model.SocialLink
import com.jksalcedo.tend.domain.usecase.AddPersonUseCase
import com.jksalcedo.tend.domain.usecase.GetPersonUseCase
import com.jksalcedo.tend.domain.usecase.UpdatePersonUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddPersonViewModel(
    private val addPersonUseCase: AddPersonUseCase,
    private val updatePersonUseCase: UpdatePersonUseCase,
    private val getPersonUseCase: GetPersonUseCase
) : ViewModel() {

    private val _existingPerson = MutableStateFlow<Person?>(null)
    val existingPerson: StateFlow<Person?> = _existingPerson.asStateFlow()

    fun loadPerson(id: Long) {
        viewModelScope.launch {
            _existingPerson.value = getPersonUseCase(id)
        }
    }

    fun addPerson(
        name: String,
        frequencyDays: Int,
        notes: String,
        phoneNumber: String?,
        email: String?,
        socialLinks: List<SocialLink>,
        events: List<PersonEvent>
    ) {
        viewModelScope.launch {
            addPersonUseCase(
                name = name,
                frequencyDays = frequencyDays,
                initialNote = notes,
                phoneNumber = phoneNumber,
                email = email,
                socialLinks = socialLinks,
                events = events
            )
        }
    }

    fun updatePerson(
        personId: Long,
        name: String,
        frequencyDays: Int,
        phoneNumber: String?,
        email: String?,
        socialLinks: List<SocialLink>,
        events: List<PersonEvent>
    ) {
        viewModelScope.launch {
            updatePersonUseCase(
                personId = personId,
                name = name,
                frequencyDays = frequencyDays,
                phoneNumber = phoneNumber,
                email = email,
                socialLinks = socialLinks,
                events = events
            )
        }
    }
}

package com.jksalcedo.tend.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.model.PersonEvent
import com.jksalcedo.tend.domain.model.SocialLink
import com.jksalcedo.tend.domain.usecase.AddPersonUseCase
import com.jksalcedo.tend.domain.usecase.ObservePersonUseCase
import com.jksalcedo.tend.domain.usecase.UpdatePersonUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddPersonViewModel(
    private val addPersonUseCase: AddPersonUseCase,
    private val updatePersonUseCase: UpdatePersonUseCase,
    private val observePersonUseCase: ObservePersonUseCase
) : ViewModel() {

    private val _existingPerson = MutableStateFlow<Person?>(null)
    val existingPerson: StateFlow<Person?> = _existingPerson.asStateFlow()

    // A live Room query, not a one-shot fetch — so if the linked-contact foreground
    // refresh updates this person's name/phone/email while this screen is open (e.g.
    // the user went to "Edit in Contacts" and back), the locked identity fields here
    // pick up the change instead of showing what was loaded when the screen opened.
    private var observeJob: Job? = null

    fun loadPerson(id: Long) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observePersonUseCase(id).collect { _existingPerson.value = it }
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

package com.jksalcedo.tend.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.tend.domain.usecase.AddPersonUseCase
import kotlinx.coroutines.launch

class AddPersonViewModel(
    private val addPersonUseCase: AddPersonUseCase
) : ViewModel() {

    fun addPerson(
        name: String,
        frequencyDays: Int,
        notes: String,
        phoneNumber: String?,
        email: String?,
        socialLinks: List<com.jksalcedo.tend.domain.model.SocialLink>,
        events: List<com.jksalcedo.tend.domain.model.PersonEvent>
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
}

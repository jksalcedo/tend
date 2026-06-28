package com.jksalcedo.tend.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.tend.domain.usecase.AddPersonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPersonViewModel @Inject constructor(
    private val addPersonUseCase: AddPersonUseCase
) : ViewModel() {

    fun addPerson(
        name: String,
        cadenceDays: Int,
        notes: String,
        phoneNumber: String?,
        email: String?,
        socialLinks: List<com.jksalcedo.tend.domain.model.SocialLink>,
        events: List<com.jksalcedo.tend.domain.model.PersonEvent>
    ) {
        viewModelScope.launch {
            addPersonUseCase(
                name = name,
                cadenceDays = cadenceDays,
                initialNote = notes,
                phoneNumber = phoneNumber,
                email = email,
                socialLinks = socialLinks,
                events = events
            )
        }
    }
}

package com.jksalcedo.crontact.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.crontact.domain.usecase.AddPersonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPersonViewModel @Inject constructor(
    private val addPersonUseCase: AddPersonUseCase
) : ViewModel() {

    fun addPerson(name: String, cadenceDays: Int, notes: String) {
        viewModelScope.launch {
            addPersonUseCase(name, cadenceDays, notes)
        }
    }
}

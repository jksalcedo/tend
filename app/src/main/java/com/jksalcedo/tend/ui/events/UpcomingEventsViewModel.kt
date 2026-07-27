package com.jksalcedo.tend.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jksalcedo.tend.domain.model.UpcomingEventItem
import com.jksalcedo.tend.domain.usecase.GetUpcomingEventsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class UpcomingEventsViewModel(
    getUpcomingEventsUseCase: GetUpcomingEventsUseCase
) : ViewModel() {

    val upcomingEvents: StateFlow<List<UpcomingEventItem>> =
        getUpcomingEventsUseCase(daysAhead = 60)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

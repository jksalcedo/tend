package com.jksalcedo.tend.domain.model

data class UpcomingEventItem(
    val personId: Long,
    val personName: String,
    val photoUri: String?,
    val localPhotoPath: String?,
    val eventLabel: String,
    val eventType: EventType,
    val eventDate: Long,
    val daysRemaining: Int,
    val formattedDate: String,
    val phoneNumber: String?,
    val email: String?
)

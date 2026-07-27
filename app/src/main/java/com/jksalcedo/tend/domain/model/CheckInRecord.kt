package com.jksalcedo.tend.domain.model

data class CheckInRecord(
    val id: Long = 0,
    val personId: Long,
    val timestamp: Long,
    val interactionType: InteractionType? = null,
    val note: String? = null
)

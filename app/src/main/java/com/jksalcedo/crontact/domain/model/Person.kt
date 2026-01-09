package com.jksalcedo.crontact.domain.model

data class Person(
    val id: Long = 0,
    val name: String,
    val photoUri: String?,
    val notes: String,
    val cadenceDays: Int,
    val lastContactedAt: Long,
    val nextReminderAt: Long,
    val isArchived: Boolean = false
)

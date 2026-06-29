package com.jksalcedo.tend.domain.model

data class Person(
    val id: Long = 0,
    val name: String,
    val photoUri: String?,
    val phoneNumber: String? = null,
    val email: String? = null,
    val events: List<PersonEvent> = emptyList(),
    val notes: List<Note> = emptyList(),
    val socialLinks: List<SocialLink> = emptyList(),
    val frequencyDays: Int,
    val lastContactedAt: Long,
    val nextReminderAt: Long,
    val isArchived: Boolean = false
)

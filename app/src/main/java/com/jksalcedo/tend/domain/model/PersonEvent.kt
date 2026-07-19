package com.jksalcedo.tend.domain.model

import java.util.UUID

data class PersonEvent(
    val id: String = UUID.randomUUID().toString(),
    val label: String, // e.g., "Birthday", "Anniversary"
    val date: Long, // Timestamp representing the date (year might be ignored for recurring)
    val type: EventType = EventType.OTHER,
    val leadTimeDays: Int = 0 // Number of days before the event to show a reminder
)

enum class EventType {
    BIRTHDAY,
    ANNIVERSARY,
    OTHER
}

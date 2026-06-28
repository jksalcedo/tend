package com.jksalcedo.tend.domain.model

import java.util.UUID

data class PersonEvent(
    val id: String = UUID.randomUUID().toString(),
    val label: String, // e.g., "Birthday", "Anniversary"
    val date: Long, // Timestamp representing the date (year might be ignored for recurring)
    val type: EventType = EventType.OTHER
)

enum class EventType {
    BIRTHDAY,
    ANNIVERSARY,
    OTHER
}

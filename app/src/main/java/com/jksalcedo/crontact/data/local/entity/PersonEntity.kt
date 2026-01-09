package com.jksalcedo.crontact.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val photoUri: String?,         // Local file path
    val notes: String,             // "Met at conference, likes coffee"
    val cadenceDays: Int,          // e.g., 14 for "every 2 weeks"
    val lastContactedAt: Long,     // Timestamp (Epoch millis)
    val nextReminderAt: Long,      // Timestamp (Calculated: last + cadence)
    val isArchived: Boolean = false
)

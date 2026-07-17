package com.jksalcedo.tend.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "check_in_history",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("personId"),
        Index("timestamp")
    ]
)
data class CheckInHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personId: Long,
    val timestamp: Long
)

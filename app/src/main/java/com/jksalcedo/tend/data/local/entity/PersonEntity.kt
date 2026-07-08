package com.jksalcedo.tend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jksalcedo.tend.domain.model.Note
import com.jksalcedo.tend.domain.model.PersonEvent
import com.jksalcedo.tend.domain.model.SocialLink

@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val photoUri: String?,
    // TODO: single-valued — see matching TODO on domain/model/Person.kt for why, and what
    // changing this would involve.
    val phoneNumber: String? = null,
    val email: String? = null,
    val events: List<PersonEvent> = emptyList(),
    val notes: List<Note> = emptyList(),
    val socialLinks: List<SocialLink> = emptyList(),
    val frequencyDays: Int,
    val lastContactedAt: Long,
    val nextReminderAt: Long,
    val isArchived: Boolean = false,
    val nativeLookupKey: String? = null,
    val nativeContactId: Long? = null,
    val isDeviceLinkBroken: Boolean = false,
    val localPhotoPath: String? = null,
    val tags: List<String> = emptyList()
)

package com.jksalcedo.tend.domain.model

data class Person(
    val id: Long = 0,
    val name: String,
    val photoUri: String?,
    // Deliberately single-valued, unlike native contacts which allow several typed phone
    // numbers/emails per contact (Home/Work/Mobile/Other, one marked primary) — keeping a
    // simpler UI was chosen over full multi-value fidelity (see the README's Non-Goals).
    // Importing from native contacts keeps only the contact's designated primary (or an
    // arbitrary one if none is marked) — see NativeContactsDataSource.
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

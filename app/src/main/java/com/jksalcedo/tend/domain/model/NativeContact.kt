package com.jksalcedo.tend.domain.model

data class NativeContact(
    val lookupKey: String,
    val contactId: Long,
    val name: String,
    val phoneNumber: String?,
    val email: String?,
    val photoUri: String?
)

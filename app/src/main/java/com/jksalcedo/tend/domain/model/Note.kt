package com.jksalcedo.tend.domain.model

import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

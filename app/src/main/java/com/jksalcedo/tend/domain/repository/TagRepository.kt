package com.jksalcedo.tend.domain.repository

import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun observeAllTags(): Flow<List<String>>
    suspend fun ensureTagExists(name: String)
    suspend fun deleteTag(name: String)
}

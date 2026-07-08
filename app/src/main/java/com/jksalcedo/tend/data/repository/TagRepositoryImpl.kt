package com.jksalcedo.tend.data.repository

import com.jksalcedo.tend.data.local.TagDao
import com.jksalcedo.tend.data.local.entity.TagEntity
import com.jksalcedo.tend.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow

class TagRepositoryImpl(
    private val dao: TagDao
) : TagRepository {

    override fun observeAllTags(): Flow<List<String>> {
        return dao.observeAllTags()
    }

    override suspend fun ensureTagExists(name: String) {
        if (name.isBlank()) return
        dao.insertTag(TagEntity(name))
    }

    override suspend fun deleteTag(name: String) {
        dao.deleteTag(name)
    }
}

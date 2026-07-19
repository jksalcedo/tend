package com.jksalcedo.tend.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jksalcedo.tend.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT name FROM tags ORDER BY name ASC")
    fun observeAllTags(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity)

    @Query("DELETE FROM tags WHERE name = :name")
    suspend fun deleteTag(name: String)
}

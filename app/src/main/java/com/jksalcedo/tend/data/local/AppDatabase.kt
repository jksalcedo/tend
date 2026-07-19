package com.jksalcedo.tend.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jksalcedo.tend.data.local.entity.PersonEntity
import com.jksalcedo.tend.data.local.entity.TagEntity

@Database(
    entities = [PersonEntity::class, TagEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun checkInDao(): CheckInDao
    abstract fun tagDao(): TagDao
}

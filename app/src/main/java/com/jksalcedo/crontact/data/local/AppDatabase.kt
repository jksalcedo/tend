package com.jksalcedo.crontact.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jksalcedo.crontact.data.local.entity.PersonEntity

@Database(entities = [PersonEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun checkInDao(): CheckInDao
}

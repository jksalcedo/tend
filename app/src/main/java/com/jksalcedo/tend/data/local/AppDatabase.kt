package com.jksalcedo.tend.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jksalcedo.tend.data.local.entity.PersonEntity
import com.jksalcedo.tend.data.local.entity.TagEntity

import com.jksalcedo.tend.data.local.entity.CheckInHistoryEntity

@Database(
    entities = [PersonEntity::class, TagEntity::class, CheckInHistoryEntity::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun checkInDao(): CheckInDao
    abstract fun tagDao(): TagDao
    abstract fun checkInHistoryDao(): CheckInHistoryDao
}

val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `check_in_history` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `personId` INTEGER NOT NULL,
                `timestamp` INTEGER NOT NULL,
                FOREIGN KEY(`personId`) REFERENCES `people`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_check_in_history_personId` ON `check_in_history` (`personId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_check_in_history_timestamp` ON `check_in_history` (`timestamp`)")
    }
}

val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE people ADD COLUMN reminderWindowDays INTEGER NOT NULL DEFAULT 0")
    }
}

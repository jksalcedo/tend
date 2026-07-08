package com.jksalcedo.tend.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Covers the columns added for device-contact sync (Case 1/2 linking, broken-link
// detection, local photo caching). All additive and nullable/defaulted, so no data
// transformation is needed beyond the new columns existing. This is the only migration
// path any real released version of the app has ever needed.
val MIGRATION_3_5 = object : Migration(3, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE people ADD COLUMN nativeLookupKey TEXT")
        db.execSQL("ALTER TABLE people ADD COLUMN nativeContactId INTEGER")
        db.execSQL("ALTER TABLE people ADD COLUMN isDeviceLinkBroken INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE people ADD COLUMN localPhotoPath TEXT")
    }
}

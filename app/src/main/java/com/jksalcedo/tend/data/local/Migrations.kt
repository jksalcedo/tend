package com.jksalcedo.tend.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Covers the columns added for device-contact sync (Case 1/2 linking, broken-link
// detection, local photo caching). All additive and nullable/defaulted, so no data
// transformation is needed beyond the new columns existing. This is the only migration
// path any real released version of the app has ever needed — no released build has ever
// shipped with a `duplicateOfPersonId` column (that field only ever existed transiently
// during this PR's own development, see MIGRATION_5_6).
val MIGRATION_3_5 = object : Migration(3, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE people ADD COLUMN nativeLookupKey TEXT")
        db.execSQL("ALTER TABLE people ADD COLUMN nativeContactId INTEGER")
        db.execSQL("ALTER TABLE people ADD COLUMN isDeviceLinkBroken INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE people ADD COLUMN localPhotoPath TEXT")
    }
}

// Drops the short-lived `duplicateOfPersonId` column (replaced by a live query for people
// sharing a nativeLookupKey — see ObserveDuplicatePeopleUseCase — rather than a stored
// pairwise pointer). Uses the create-copy-drop-rename pattern instead of `ALTER TABLE
// DROP COLUMN` since the latter needs SQLite 3.35+, which isn't guaranteed across every
// Android version this app supports.
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE people_new (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                photoUri TEXT,
                phoneNumber TEXT,
                email TEXT,
                events TEXT NOT NULL,
                notes TEXT NOT NULL,
                socialLinks TEXT NOT NULL,
                frequencyDays INTEGER NOT NULL,
                lastContactedAt INTEGER NOT NULL,
                nextReminderAt INTEGER NOT NULL,
                isArchived INTEGER NOT NULL,
                nativeLookupKey TEXT,
                nativeContactId INTEGER,
                isDeviceLinkBroken INTEGER NOT NULL,
                localPhotoPath TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO people_new (id, name, photoUri, phoneNumber, email, events, notes,
                socialLinks, frequencyDays, lastContactedAt, nextReminderAt, isArchived,
                nativeLookupKey, nativeContactId, isDeviceLinkBroken, localPhotoPath)
            SELECT id, name, photoUri, phoneNumber, email, events, notes,
                socialLinks, frequencyDays, lastContactedAt, nextReminderAt, isArchived,
                nativeLookupKey, nativeContactId, isDeviceLinkBroken, localPhotoPath
            FROM people
            """.trimIndent()
        )
        db.execSQL("DROP TABLE people")
        db.execSQL("ALTER TABLE people_new RENAME TO people")
    }
}

package com.jksalcedo.tend.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Everything this PR's schema changes need, in one step — device-contact sync (Case 1/2
// linking, broken-link detection, local photo caching) plus free-form tags (feature 06),
// condensed into a single migration since none of this has landed on main yet: no real
// released version of the app has ever been beyond version 3, so there's no reason to
// preserve an intermediate step nobody's database ever passed through.
//
// The `tags` column mirrors the existing notes/events/socialLinks JSON-list pattern. The
// separate `tags` table is the persisted pool of every known tag name, independent of who
// currently wears it (see the tag-pool persistence design decision in
// docs/specs/contact-sync/README.md) — seeded here for upgrading installs; a fresh install
// seeds it via RoomDatabase.Callback.onCreate instead, since this migration never runs for a
// database created at version 6 directly.
val MIGRATION_3_6 = object : Migration(3, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE people ADD COLUMN nativeLookupKey TEXT")
        db.execSQL("ALTER TABLE people ADD COLUMN nativeContactId INTEGER")
        db.execSQL("ALTER TABLE people ADD COLUMN isDeviceLinkBroken INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE people ADD COLUMN localPhotoPath TEXT")
        db.execSQL("ALTER TABLE people ADD COLUMN tags TEXT NOT NULL DEFAULT '[]'")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS tags (
                name TEXT NOT NULL PRIMARY KEY
            )
            """.trimIndent()
        )
        db.execSQL("INSERT OR IGNORE INTO tags (name) VALUES ('Family')")
        db.execSQL("INSERT OR IGNORE INTO tags (name) VALUES ('Friend')")
    }
}

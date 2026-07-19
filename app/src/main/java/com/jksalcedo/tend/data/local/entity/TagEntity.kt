package com.jksalcedo.tend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// The persisted tag pool/catalog — deliberately separate from Person.tags, which only
// records who currently wears which tag. A tag row here can outlive every person having
// it; it's only removed by an explicit delete (see DeleteTagUseCase).
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val name: String
)

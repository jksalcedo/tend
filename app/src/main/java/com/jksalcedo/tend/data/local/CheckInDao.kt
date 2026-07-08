package com.jksalcedo.tend.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jksalcedo.tend.data.local.entity.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Query("SELECT * FROM people WHERE isArchived = 0 ORDER BY nextReminderAt ASC")
    fun getAllPeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE isArchived = 1 ORDER BY nextReminderAt ASC")
    fun getArchivedPeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getPersonById(id: Long): PersonEntity?

    @Query("SELECT * FROM people WHERE id = :id")
    fun observePersonById(id: Long): Flow<PersonEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity)

    @Update
    suspend fun updatePerson(person: PersonEntity)

    @Query("DELETE FROM people WHERE id = :id")
    suspend fun deletePerson(id: Long)

    @Query("SELECT nativeLookupKey FROM people WHERE nativeLookupKey IS NOT NULL")
    suspend fun getLinkedLookupKeys(): List<String>

    @Query("SELECT * FROM people WHERE nativeLookupKey IS NOT NULL")
    suspend fun getLinkedPeople(): List<PersonEntity>

    // Regardless of archive status — needed by DeleteTagUseCase's cascade, which must strip
    // a deleted tag from every person wearing it, not just the ones currently visible on Home.
    @Query("SELECT * FROM people")
    suspend fun getEveryPerson(): List<PersonEntity>

    // Duplicates are derived live from shared nativeLookupKey rather than stored as a
    // pairwise FK — this naturally stays correct (symmetric, transitive across any group
    // size, self-clearing on unlink) with no separate flag to keep in sync.
    @Query("SELECT * FROM people WHERE nativeLookupKey = :lookupKey AND id != :excludeId")
    fun observeDuplicatesOf(lookupKey: String, excludeId: Long): Flow<List<PersonEntity>>
}

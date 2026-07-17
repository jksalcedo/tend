package com.jksalcedo.tend.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jksalcedo.tend.data.local.entity.CheckInHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(history: CheckInHistoryEntity)

    // Gets the count of unique people checked in with since the given timestamp
    @Query("SELECT COUNT(DISTINCT personId) FROM check_in_history WHERE timestamp >= :sinceTimestamp")
    fun getUniquePeopleCheckedInCountSince(sinceTimestamp: Long): Flow<Int>
}

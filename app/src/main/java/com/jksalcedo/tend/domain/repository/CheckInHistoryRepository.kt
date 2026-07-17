package com.jksalcedo.tend.domain.repository

import kotlinx.coroutines.flow.Flow

interface CheckInHistoryRepository {
    suspend fun recordCheckIn(personId: Long, timestamp: Long)
    fun getUniquePeopleCheckedInCountSince(sinceTimestamp: Long): Flow<Int>
}

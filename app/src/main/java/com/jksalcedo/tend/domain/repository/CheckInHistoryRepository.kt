package com.jksalcedo.tend.domain.repository

import com.jksalcedo.tend.domain.model.CheckInRecord
import com.jksalcedo.tend.domain.model.InteractionType
import kotlinx.coroutines.flow.Flow

interface CheckInHistoryRepository {
    suspend fun recordCheckIn(
        personId: Long,
        timestamp: Long,
        interactionType: InteractionType? = null,
        note: String? = null
    )
    fun getUniquePeopleCheckedInCountSince(sinceTimestamp: Long): Flow<Int>
    fun getHistoryForPerson(personId: Long): Flow<List<CheckInRecord>>
    suspend fun deleteCheckIn(id: Long)
}

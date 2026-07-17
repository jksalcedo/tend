package com.jksalcedo.tend.data.repository

import com.jksalcedo.tend.data.local.CheckInHistoryDao
import com.jksalcedo.tend.data.local.entity.CheckInHistoryEntity
import com.jksalcedo.tend.domain.repository.CheckInHistoryRepository
import kotlinx.coroutines.flow.Flow

class CheckInHistoryRepositoryImpl(
    private val dao: CheckInHistoryDao
) : CheckInHistoryRepository {
    override suspend fun recordCheckIn(personId: Long, timestamp: Long) {
        dao.insertCheckIn(
            CheckInHistoryEntity(
                personId = personId,
                timestamp = timestamp
            )
        )
    }

    override fun getUniquePeopleCheckedInCountSince(sinceTimestamp: Long): Flow<Int> {
        return dao.getUniquePeopleCheckedInCountSince(sinceTimestamp)
    }
}

package com.jksalcedo.tend.data.repository

import com.jksalcedo.tend.data.local.CheckInHistoryDao
import com.jksalcedo.tend.data.local.entity.CheckInHistoryEntity
import com.jksalcedo.tend.domain.model.CheckInRecord
import com.jksalcedo.tend.domain.model.InteractionType
import com.jksalcedo.tend.domain.repository.CheckInHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CheckInHistoryRepositoryImpl(
    private val dao: CheckInHistoryDao
) : CheckInHistoryRepository {
    override suspend fun recordCheckIn(
        personId: Long,
        timestamp: Long,
        interactionType: InteractionType?,
        note: String?
    ) {
        dao.insertCheckIn(
            CheckInHistoryEntity(
                personId = personId,
                timestamp = timestamp,
                interactionType = interactionType?.name,
                note = note
            )
        )
    }

    override fun getUniquePeopleCheckedInCountSince(sinceTimestamp: Long): Flow<Int> {
        return dao.getUniquePeopleCheckedInCountSince(sinceTimestamp)
    }

    override fun getHistoryForPerson(personId: Long): Flow<List<CheckInRecord>> {
        return dao.getHistoryForPerson(personId).map { entities ->
            entities.map { entity ->
                CheckInRecord(
                    id = entity.id,
                    personId = entity.personId,
                    timestamp = entity.timestamp,
                    interactionType = InteractionType.fromString(entity.interactionType),
                    note = entity.note
                )
            }
        }
    }

    override suspend fun deleteCheckIn(id: Long) {
        dao.deleteCheckIn(id)
    }
}

package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.repository.CheckInHistoryRepository
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class GetWeeklyInsightsUseCase(
    private val repository: CheckInHistoryRepository
) {
    operator fun invoke(): Flow<Int> {
        // Last 7 days
        val sinceTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        return repository.getUniquePeopleCheckedInCountSince(sinceTimestamp)
    }
}

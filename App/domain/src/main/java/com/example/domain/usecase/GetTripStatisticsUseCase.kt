package com.example.domain.usecase

import com.example.domain.model.TripStatistics
import com.example.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTripStatisticsUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    operator fun invoke(isMonthly: Boolean): Flow<List<TripStatistics>> {
        return if (isMonthly) repository.getMonthlyStats() else repository.getDailyStats()
    }
}
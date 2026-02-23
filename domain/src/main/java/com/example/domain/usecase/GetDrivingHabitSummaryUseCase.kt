package com.example.domain.usecase

import com.example.domain.model.DrivingHabitSummary
import com.example.domain.repository.HistoryRepository
import javax.inject.Inject

class GetDrivingHabitSummaryUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke(): DrivingHabitSummary {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)

        return repository.getHistorySince(sevenDaysAgo)
    }
}
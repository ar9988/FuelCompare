package com.example.domain.usecase

import com.example.domain.repository.HistoryRepository
import com.example.domain.service.TripManager
import javax.inject.Inject

class SaveTripSummaryUseCase @Inject constructor(
    private val tripManager: TripManager,
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke() {
        val summary = tripManager.getSummary()

        // 최소 주행 거리(예: 10m) 조건 체크 후 저장
        if (summary.totalDistance > 10.0) {
            historyRepository.saveTrip(summary)
        }
    }
}
package com.example.domain.usecase

import com.example.domain.model.TripHistory
import com.example.domain.repository.CarRepository
import com.example.domain.repository.HistoryRepository
import javax.inject.Inject

class SaveTripSummaryUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val recordDrivingEventUseCase: RecordDrivingEventUseCase,
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke() {
        val totalDist = carRepository.getTotalDistance()

        println("DEBUG_SAVE: Total distance calculated: $totalDist")
        if (totalDist > 10.0) { // 최소 주행거리
            val currentEfficiency = carRepository.fuelEfficiency.value
            val (accelCount, brakeCount) = recordDrivingEventUseCase.getCounts()
            val history = TripHistory(
                date = System.currentTimeMillis(),
                avgEfficiency = currentEfficiency,
                harshAccelCount = accelCount,
                harshBrakeCount = brakeCount,
                totalDistance = totalDist
            )

            // 2. 리포지토리를 통해 저장
            historyRepository.saveTrip(history)

            // 3. 저장 성공 후 카운터 초기화 (필요 시)
            recordDrivingEventUseCase.reset()
        }
    }
}
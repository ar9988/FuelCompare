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
        // 1. 필요한 모든 데이터를 유즈케이스 내부에서 수집
        val totalDist = carRepository.getTotalDistance()

        // 데이터가 유의미할 때만 저장하는 '비즈니스 규칙'도 여기에 위치
        if (totalDist > 100.0) {
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
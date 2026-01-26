package com.example.domain.usecase

import com.example.domain.model.TripEndResult
import com.example.domain.model.VehicleIgnitionState
import com.example.domain.repository.CarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MonitorTripLifecycleUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val saveTripSummaryUseCase: SaveTripSummaryUseCase
) {
    operator fun invoke(): Flow<TripEndResult> = carRepository.ignitionState
        .filter { state ->
            // 시동이 OFF 또는 LOCK이 된 경우만 필터링
            state == VehicleIgnitionState.OFF || state == VehicleIgnitionState.LOCK
        }
        .distinctUntilChanged() // 상태가 확실히 변했을 때만 실행
        .map {
            try {
                saveTripSummaryUseCase()
                TripEndResult.Success // 저장 성공 이벤트 반환
            } catch (e: Exception) {
                TripEndResult.Error(e.message ?: "Unknown Error")
            }
        }
}
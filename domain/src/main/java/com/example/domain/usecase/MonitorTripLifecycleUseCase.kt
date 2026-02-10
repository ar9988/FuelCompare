package com.example.domain.usecase

import com.example.domain.model.TripEndResult
import com.example.domain.model.VehicleGearState
import com.example.domain.repository.CarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MonitorTripLifecycleUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val saveTripSummaryUseCase: SaveTripSummaryUseCase
) {
    private var previousGear: VehicleGearState? = null

    operator fun invoke(): Flow<TripEndResult> = carRepository.gearState
        .map { currentGear ->
            val isRealEnd = (previousGear == VehicleGearState.DRIVE ||
                    previousGear == VehicleGearState.REVERSE ||
                    previousGear == VehicleGearState.NEUTRAL) &&
                    currentGear == VehicleGearState.PARK

            previousGear = currentGear
            isRealEnd
        }
        .filter { it } // 위 조건이 true(주행 종료)일 때만 통과
        .map {
            try {
                saveTripSummaryUseCase()
                TripEndResult.Success
            } catch (e: Exception) {
                TripEndResult.Error(e.message ?: "Unknown Error")
            }
        }
}
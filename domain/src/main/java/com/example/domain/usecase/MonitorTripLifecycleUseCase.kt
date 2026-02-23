package com.example.domain.usecase

import com.example.domain.model.TripState
import com.example.domain.model.VehicleGearState
import com.example.domain.repository.CarRepository
import com.example.domain.service.TripManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class MonitorTripLifecycleUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val tripManager: TripManager,
    private val saveTripSummaryUseCase: SaveTripSummaryUseCase,
) {
    private var previousGear: VehicleGearState = VehicleGearState.UNDEFINED

    operator fun invoke(): Flow<TripState> = carRepository.gearState.map { currentGear ->
        // 1. 상태 판정 로직

        val state = when {
            (previousGear == VehicleGearState.PARK || previousGear == VehicleGearState.UNDEFINED) &&
                    currentGear == VehicleGearState.DRIVE -> {
                TripState.Driving
            }
            previousGear == VehicleGearState.DRIVE && currentGear == VehicleGearState.PARK -> {
                TripState.Finished(tripManager.getSummary())
            }
            else -> {
                TripState.Idle
            }
        }

        // 2. 이전 기어 업데이트 (UNDEFINED 제외)
        if (currentGear != VehicleGearState.UNDEFINED) {
            previousGear = currentGear
        }

        state
    }.onEach { state ->
        // 3. 사이드 이펙트 처리
        when (state) {
            is TripState.Driving -> {
                tripManager.reset(tripManager.latestFuelLevel)
                tripManager.updateTripState(TripState.Driving)
            }
            is TripState.Finished -> {
                saveTripSummaryUseCase()
                tripManager.updateTripState(state)
            }
            is TripState.Idle -> {
                tripManager.updateTripState(TripState.Idle)
            }
        }
    }
}

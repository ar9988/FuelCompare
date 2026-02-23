package com.example.domain.usecase

import com.example.domain.model.FuelEfficiencyState
import com.example.domain.service.TripManager
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class GetFuelEfficiencyUseCase @Inject constructor(
    private val tripManager: TripManager
) {
    operator fun invoke(): Flow<FuelEfficiencyState> {
        return tripManager.realTimeStatus
            .map { status ->
                status.efficiency
            }
            .distinctUntilChanged()
    }
}
package com.example.domain.usecase

import com.example.domain.model.TripState
import com.example.domain.service.TripManager
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetTripStateUseCase @Inject constructor(
    private val tripManager: TripManager
) {
    operator fun invoke(): StateFlow<TripState> = tripManager.tripState
}

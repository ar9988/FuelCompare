package com.example.domain.usecase

import com.example.domain.service.TripManager
import javax.inject.Inject

class GetTotalDistanceUseCase @Inject constructor(
    private val tripManager: TripManager
) {
    operator fun invoke() : Double = tripManager.totalDistanceMeters
}
package com.example.domain.usecase

import com.example.domain.repository.CarRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTotalDistanceUseCase @Inject constructor(
    private val carRepository: CarRepository
) {
    operator fun invoke() : Double = carRepository.getTotalDistance()
}
package com.example.domain.usecase

import com.example.domain.repository.CarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetFuelEfficiencyUseCase @Inject constructor(
    private val carRepository: CarRepository
) {
    operator fun invoke() : Flow<Float> = carRepository.getEfficiency()
}
package com.example.domain.usecase

import com.example.domain.model.VehicleGearState
import com.example.domain.repository.CarRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetGearUseCase @Inject constructor(
    private val carRepository: CarRepository
){
    operator fun invoke() : StateFlow<VehicleGearState> = carRepository.gearState
}
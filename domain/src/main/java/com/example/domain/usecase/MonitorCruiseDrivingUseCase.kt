package com.example.domain.usecase

import com.example.domain.model.DrivingAlert
import com.example.domain.repository.CarRepository
import com.example.domain.service.SpeechService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MonitorCruiseDrivingUseCase @Inject constructor(
    private val carRepository: CarRepository,
) {
    private val speedHistory = mutableListOf<Float>()

    operator fun invoke(): Flow<DrivingAlert> = carRepository.observeSpeed().map { speed ->
        speedHistory.add(speed)
        if (speedHistory.size > 20) speedHistory.removeAt(0) // 최근 약 2~4초 데이터

        val isHighSpeed = speed > 16.6f // 60km/h 이상
        val isSteady = speedHistory.size >= 20 &&
                (speedHistory.maxOrNull()!! - speedHistory.minOrNull()!! < 0.5f) // 변화폭 2km/h 이내

        val isCruising = isHighSpeed && isSteady
        if (isCruising) {
            DrivingAlert.CONSTANT_SPEED_DRIVING
        }
        DrivingAlert.NORMAL
    }.distinctUntilChanged()
}
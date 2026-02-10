package com.example.domain.usecase

import com.example.domain.model.DrivingAlert
import com.example.domain.repository.CarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MonitorDrivingHabitUseCase @Inject constructor(
    private val carRepository: CarRepository,
) {
    private var lastSpeed = -1f
    private var lastTimestamp = 0L
    private var smoothedSpeed = -1f
    private val alpha = 0.1f

    operator fun invoke(): Flow<DrivingAlert> =
        carRepository.observeSpeed().map { rawSpeed ->
            val currentTime = System.currentTimeMillis()

            // 1. 필터링
            smoothedSpeed = if (smoothedSpeed < 0) rawSpeed else smoothedSpeed + alpha * (rawSpeed - smoothedSpeed)

            // 2. 가속도 계산
            val acceleration = calculateAcceleration(smoothedSpeed, currentTime)

            // 3. 상태 판정
            val alert = when {
                acceleration > 3.0f -> DrivingAlert.HARSH_ACCEL
                acceleration < -3.0f -> DrivingAlert.HARSH_BRAKE
                else -> DrivingAlert.NORMAL
            }
            alert
        }.distinctUntilChanged()

    private fun calculateAcceleration(currentSpeed: Float, currentTime: Long): Float {
        if (lastTimestamp == 0L || lastSpeed < 0f) {
            lastSpeed = currentSpeed
            lastTimestamp = currentTime
            return 0f
        }
        val deltaTimeSeconds = (currentTime - lastTimestamp) / 1000f
        if (deltaTimeSeconds < 0.1f) return 0f

        val acceleration = (currentSpeed - lastSpeed) / deltaTimeSeconds

        lastSpeed = currentSpeed
        lastTimestamp = currentTime
        return acceleration
    }
}
package com.example.domain.usecase

import com.example.domain.model.DrivingAlert
import com.example.domain.repository.CarRepository
import com.example.domain.service.SpeechService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class MonitorIdlingUseCase @Inject constructor(
    private val carRepository: CarRepository,
) {
    private var idlingStartTime = 0L
    private var hasAlertedForCurrentSession = false

    operator fun invoke(): Flow<DrivingAlert> = combine(
        carRepository.observeSpeed(),
        carRepository.observeEngineRpm()
    ) { speed, rpm ->
        // 조건: 속도 0 + 시동 켜짐(RPM > 500)
        val isIdling = speed < 0.1f && rpm > 500f

        if (isIdling) {
            if (idlingStartTime == 0L) idlingStartTime = System.currentTimeMillis()
            val duration = (System.currentTimeMillis() - idlingStartTime) / 1000

            if (duration >= 60_000L && !hasAlertedForCurrentSession) {
                hasAlertedForCurrentSession = true
                DrivingAlert.EXCESSIVE_IDLING
            } else {
                DrivingAlert.NORMAL
            }
        } else {
            idlingStartTime = 0L
            hasAlertedForCurrentSession = false
            DrivingAlert.NORMAL
        }
    }.distinctUntilChanged()
}
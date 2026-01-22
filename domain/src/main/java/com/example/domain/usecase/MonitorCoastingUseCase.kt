package com.example.domain.usecase

import com.example.domain.model.DrivingAlert
import com.example.domain.repository.CarRepository
import com.example.domain.service.SpeechService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class MonitorCoastingUseCase @Inject constructor(
    private val carRepository: CarRepository,
) {
    private var coastingStartTime = 0L
    private var hasAlertedForCurrentSession = false

    operator fun invoke(): Flow<DrivingAlert> = combine(
        carRepository.observeSpeed(),
        carRepository.observeEngineRpm(),
        carRepository.observeGear()
    ) { speed, rpm, gear ->
        // 조건: 속도 30km/h(8.3m/s) 이상 + 기어 Drive(8) + RPM 낮음(가속 안함)
        val isCoasting = speed > 8.3f && gear == 8 && rpm < 1100f

        if (isCoasting) {
            if (coastingStartTime == 0L) coastingStartTime = System.currentTimeMillis()
            if (System.currentTimeMillis() - coastingStartTime >= 3000L) { // 3초 이상 유지 시
                hasAlertedForCurrentSession = true
                DrivingAlert.INERTIAL_DRIVING
            } else{
                DrivingAlert.NORMAL
            }
        } else {
            coastingStartTime = 0L
            hasAlertedForCurrentSession = false
            DrivingAlert.NORMAL
        }
    }.distinctUntilChanged()
}
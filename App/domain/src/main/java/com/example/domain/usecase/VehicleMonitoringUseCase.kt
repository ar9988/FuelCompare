package com.example.domain.usecase

import com.example.domain.model.SpeechTag
import com.example.domain.model.FuelEfficiencyState
import com.example.domain.model.TripState
import com.example.domain.model.VehicleGearState
import com.example.domain.model.VehicleStatus
import com.example.domain.repository.CarRepository
import com.example.domain.service.TripManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleMonitoringUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val tripManager: TripManager
) {
    // 가속도 계산용 변수
    private var lastSpeedMps = -1f
    private var smoothedSpeedMps = -1f
    private val alpha = 0.1f

    // 타이머용 변수
    private var coastingStartTime = 0L
    private var idlingStartTime = 0L
    private var cruiseStartTime = 0L
    private var speedHistory = mutableListOf<Float>()

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<VehicleStatus> = carRepository.observeSpeed()
        .flatMapLatest { speed ->
            combine(
                carRepository.observeFuelLevel().take(1),
                carRepository.observeEngineRpm().take(1),
                carRepository.gearState.take(1),
                tripManager.tripState.take(1)
            ) { fuel, rpm, gear, tripState ->
                processVehicleData(speed, fuel, rpm, gear, tripState)
            }
        }.distinctUntilChanged()

    private fun processVehicleData(
        speed: Float,
        fuel: Float,
        rpm: Float,
        gear: VehicleGearState,
        tripState: TripState
    ): VehicleStatus {

        // 1. 주행 중이 아니면 모든 상태 리셋
        if (tripState !is TripState.Driving) {
            resetInternalState()
            return VehicleStatus(FuelEfficiencyState.Initializing, SpeechTag.NORMAL)
        }

        val currentTime = System.currentTimeMillis()
        if (tripManager.lastSpeedTimestamp == 0L) {
            tripManager.lastSpeedTimestamp = currentTime
            lastSpeedMps = speed / 3.6f
            return VehicleStatus(FuelEfficiencyState.Initializing, SpeechTag.NORMAL)
        }

        // 2. 공통 시간 간격(Delta) 계산
        val deltaMillis = currentTime - tripManager.lastSpeedTimestamp
        val deltaSeconds = deltaMillis / 1000.0
        tripManager.lastSpeedTimestamp = currentTime

        // 3. 주행 거리 계산 (m/s 변환 필수)
        if (deltaSeconds in 0.001..5.0) {
            val currentSpeedMps = speed / 3.6f
            tripManager.totalDistanceMeters += (currentSpeedMps * deltaSeconds)
        }

        // 4. 개별 모니터링 로직 실행
        updateIdling(speed, rpm, deltaMillis, currentTime)
        updateCoasting(speed, rpm, gear, deltaMillis, currentTime)
        updateCruise(speed, deltaMillis,currentTime)

        val habitAlert = updateDrivingHabit(speed, currentTime, deltaSeconds.toFloat())
        val efficiency = calculateEfficiency(fuel)

        // 우선순위가 높은 알림(급가속/급감속)이 있다면 해당 알림을 반환, 아니면 상태 알림 반환
        val finalAlert = if (habitAlert != SpeechTag.NORMAL) habitAlert else getCurrentStateAlert()
        val status = VehicleStatus(efficiency, finalAlert)
        tripManager.updateRealTimeStatus(status)
        return status
    }

    // --- 세부 로직 함수들 ---

    private fun updateIdling(speed: Float, rpm: Float, delta: Long, now: Long) {
        if (speed < 1.0f && rpm > 500f) {
            tripManager.addIdlingTime(delta)
            if (idlingStartTime == 0L) idlingStartTime = now
        } else {
            idlingStartTime = 0L
            tripManager.stopIdling()
        }
    }

    private fun updateCoasting(speed: Float, rpm: Float, gear: VehicleGearState, delta: Long, now: Long) {
        // 타력 주행 조건: 30km/h 이상 + D기어 + 가속 안함(RPM 저하)
        if (speed > 30f && gear == VehicleGearState.DRIVE && rpm < 1100f) {
            tripManager.addCoastingTime(delta)
            if (coastingStartTime == 0L) coastingStartTime = now
        } else {
            coastingStartTime = 0L
            tripManager.stopCoasting()
        }
    }

    private fun updateCruise(speed: Float, delta: Long,now: Long) {
        if (speed > 1.0f) {
            speedHistory.add(speed)
            if (speedHistory.size > 20) speedHistory.removeAt(0)
        }

        val isHighSpeed = speed > 60f // 60km/h 이상
        val isSteady = speedHistory.size >= 20 && (speedHistory.maxOrNull()!! - speedHistory.minOrNull()!! < 2.0f)

        if (isHighSpeed && isSteady) {
            tripManager.addCruiseTime(delta)
            if (cruiseStartTime == 0L) cruiseStartTime = now
        }else {
            cruiseStartTime = 0L
            tripManager.stopCruise()
        }
    }

    private fun updateDrivingHabit(rawSpeed: Float, now: Long, dt: Float): SpeechTag {
        val currentSpeedMps = rawSpeed / 3.6f
        smoothedSpeedMps = if (smoothedSpeedMps < 0) currentSpeedMps else smoothedSpeedMps + alpha * (currentSpeedMps - smoothedSpeedMps)

        val acceleration = (smoothedSpeedMps - lastSpeedMps) / dt
        lastSpeedMps = smoothedSpeedMps

        return when {
            acceleration > 3.0f -> { // 약 11km/h/s 가속
                if (now - tripManager.lastAccelRecordTime > 3000L) {
                    tripManager.harshAccelCount++
                    tripManager.lastAccelRecordTime = now
                    SpeechTag.HARSH_ACCEL
                } else SpeechTag.NORMAL
            }
            acceleration < -3.5f -> { // 급브레이크
                if (now - tripManager.lastAccelRecordTime > 3000L) {
                    tripManager.harshBrakeCount++
                    tripManager.lastAccelRecordTime = now
                    SpeechTag.HARSH_BRAKE
                } else SpeechTag.NORMAL
            }
            else -> SpeechTag.NORMAL
        }
    }

    private fun calculateEfficiency(fuel: Float): FuelEfficiencyState {
        if (tripManager.startFuelLevel == null || fuel > tripManager.startFuelLevel!!) {
            tripManager.startFuelLevel = fuel
        }
        val consumed = tripManager.startFuelLevel!! - fuel
        val distanceKm = tripManager.totalDistanceMeters / 1000.0
        val fuelLiters = consumed / 1000.0

        if (distanceKm < 0.02 || fuelLiters < 0.001) return FuelEfficiencyState.Initializing

        val rawEff = (distanceKm / fuelLiters).toFloat().coerceIn(0.1f, 30.0f)
        tripManager.smoothedEfficiency = if (tripManager.smoothedEfficiency == 0f) rawEff
        else tripManager.smoothedEfficiency + 0.15f * (rawEff - tripManager.smoothedEfficiency)

        return FuelEfficiencyState.Ready(tripManager.smoothedEfficiency)
    }

    private fun getCurrentStateAlert(): SpeechTag {
        val now = System.currentTimeMillis()
        return when {
            idlingStartTime != 0L && (now - idlingStartTime) >= 5000L -> SpeechTag.EXCESSIVE_IDLING
            coastingStartTime != 0L && (now - coastingStartTime) >= 3000L -> SpeechTag.INERTIAL_DRIVING
            cruiseStartTime != 0L && (now - cruiseStartTime) >= 3000L -> SpeechTag.CONSTANT_SPEED_DRIVING
            else -> SpeechTag.NORMAL
        }
    }

    private fun resetInternalState() {
        tripManager.lastSpeedTimestamp = 0L
        lastSpeedMps = -1f
        smoothedSpeedMps = -1f
        coastingStartTime = 0L
        idlingStartTime = 0L
        speedHistory.clear()
    }
}
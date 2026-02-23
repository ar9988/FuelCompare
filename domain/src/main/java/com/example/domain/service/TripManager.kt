package com.example.domain.service

import com.example.domain.model.TripHistory
import com.example.domain.model.TripState
import com.example.domain.model.VehicleStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripManager @Inject constructor() {
    // --- 기본 주행 데이터 ---
    var startFuelLevel: Float? = null
    var startTime: Long? = null
    var latestFuelLevel: Float? = null
    var totalDistanceMeters: Double = 0.0
    var lastSpeedTimestamp: Long = 0L
    var smoothedEfficiency: Float = 0f

    // 급가속/급감속 기록 방지용
    var lastAccelRecordTime: Long = 0L

    // --- 운전 습관 통계 ---
    var harshAccelCount: Int = 0
    var harshBrakeCount: Int = 0

    var totalCoastingTimeMillis: Long = 0L
    var coastingCount: Int = 0
    private var isCurrentlyCoasting: Boolean = false // 상태 추적용

    var totalCruiseTimeMillis: Long = 0L
    var cruiseCount: Int = 0
    private var isCurrentlyCruising: Boolean = false // 상태 추적용

    var totalIdlingTimeMillis: Long = 0L
    var idlingCount: Int = 0
    private var isCurrentlyIdling: Boolean = false // 상태 추적용

    private val _tripState = MutableStateFlow<TripState>(TripState.Idle)
    val tripState: StateFlow<TripState> = _tripState.asStateFlow()

    private val _realTimeStatus = MutableSharedFlow<VehicleStatus>(replay = 1)
    val realTimeStatus = _realTimeStatus.asSharedFlow()

    /**
     * 주행 세션 초기화
     */
    fun reset(currentFuel: Float?) {
        startFuelLevel = currentFuel
        latestFuelLevel = currentFuel
        startTime = System.currentTimeMillis()
        totalDistanceMeters = 0.0
        lastSpeedTimestamp = 0L
        smoothedEfficiency = 0f
        lastAccelRecordTime = 0L

        harshAccelCount = 0
        harshBrakeCount = 0

        totalCoastingTimeMillis = 0L
        coastingCount = 0
        isCurrentlyCoasting = false

        totalCruiseTimeMillis = 0L
        cruiseCount = 0
        isCurrentlyCruising = false

        totalIdlingTimeMillis = 0L
        idlingCount = 0
        isCurrentlyIdling = false
    }

    // --- 시간 누적 및 카운트 로직 (통합 유즈케이스 최적화) ---

    fun addCoastingTime(millis: Long) {
        totalCoastingTimeMillis += millis
        if (!isCurrentlyCoasting) {
            coastingCount++ // 새로 진입했을 때만 카운트 1회 증가
            isCurrentlyCoasting = true
        }
    }
    fun stopCoasting() { isCurrentlyCoasting = false }

    fun addCruiseTime(millis: Long) {
        totalCruiseTimeMillis += millis
        if (!isCurrentlyCruising) {
            cruiseCount++
            isCurrentlyCruising = true
        }
    }
    fun stopCruise() { isCurrentlyCruising = false }

    fun addIdlingTime(millis: Long) {
        totalIdlingTimeMillis += millis
        if (!isCurrentlyIdling) {
            idlingCount++
            isCurrentlyIdling = true
        }
    }
    fun stopIdling() { isCurrentlyIdling = false }

    fun updateTripState(newState: TripState) {
        _tripState.value = newState
    }

    fun updateRealTimeStatus(status: VehicleStatus) {
        _realTimeStatus.tryEmit(status)
    }

    fun getSummary(): TripHistory {
        val date = System.currentTimeMillis()
        val duration = if (startTime != null) date - startTime!! else 0L
        return TripHistory(
            date = date,
            avgEfficiency = smoothedEfficiency,
            harshAccelCount = harshAccelCount,
            harshBrakeCount = harshBrakeCount,
            totalDistance = totalDistanceMeters,
            coastingCount = coastingCount,
            cruiseCount = cruiseCount,
            coastingTimeMillis = totalCoastingTimeMillis,
            cruiseTimeMillis = totalCruiseTimeMillis,
            idlingCount = idlingCount,
            idlingTimeMillis = totalIdlingTimeMillis,
            tripDuration = duration
        )
    }
}
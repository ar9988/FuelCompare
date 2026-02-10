package com.example.data.repository

import android.car.Car
import com.example.domain.model.VehicleIgnitionState
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.util.Log
import com.example.domain.model.VehicleGearState
import com.example.domain.repository.CarRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CarRepository {

    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null

    // --- 실시간 데이터 공유를 위한 Flow들 (상태 보존형) ---
    private val _fuelEfficiency = MutableStateFlow(0f)
    override val fuelEfficiency: StateFlow<Float> = _fuelEfficiency.asStateFlow()

    private val _speedFlow = MutableSharedFlow<Float>(replay = 1)
    private val _rpmFlow = MutableSharedFlow<Float>(replay = 1)
    private val _gearFlow = MutableStateFlow(VehicleGearState.UNDEFINED)
    override val gearState: StateFlow<VehicleGearState> = _gearFlow.asStateFlow()
    private val _fuelLevelFlow = MutableSharedFlow<Float>(replay = 1)
    private val _ignitionFlow = MutableStateFlow(VehicleIgnitionState.UNDEFINED)
    override val ignitionState: StateFlow<VehicleIgnitionState> = _ignitionFlow.asStateFlow()


    private var isSessionActive = false // 현재 주행 세션이 활성 상태인지
    private var startFuelLevel: Float? = null
    private var latestFuelLevel: Float? = null
    private var totalDistanceMeters: Double = 0.0
    private var smoothedEfficiency = 0f
    private var lastSpeedTimestamp = 0L // 거리 계산 전용 타임스탬프 분리
    private val alpha = 0.3f // 값이 튀는 것을 방지하는 보정 계수 (0.1~0.3 권장)

    init {
        connectToCarService()
    }

    private fun connectToCarService() {
        car = Car.createCar(context, null, Car.CAR_WAIT_TIMEOUT_WAIT_FOREVER) { connectedCar, ready ->
            if (ready) {
                try {
                    carPropertyManager = connectedCar.getCarManager(CarPropertyManager::class.java)
                    registerAllCallbacks()
                    Log.d("CarAPI", "✅ 모든 센서 모니터링 통합 시작")
                } catch (e: Exception) {
                    Log.e("CarAPI", "❌ 매니저 획득 실패", e)
                }
            }
        }
    }

    private fun registerAllCallbacks() {
        val manager = carPropertyManager ?: return

        Log.d("CarAPI", "🔍 registerAllCallbacks 시작")

        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                synchronized(this@CarRepositoryImpl) {
                    processVehicleEvent(value)
                }
            }
            override fun onErrorEvent(propId: Int, zone: Int) {}
        }

        // 모든 필요한 센서 등록
        val properties = listOf(
            VehiclePropertyIds.PERF_VEHICLE_SPEED to CarPropertyManager.SENSOR_RATE_UI,
            VehiclePropertyIds.FUEL_LEVEL to CarPropertyManager.SENSOR_RATE_NORMAL,
            VehiclePropertyIds.ENGINE_RPM to CarPropertyManager.SENSOR_RATE_UI,
            VehiclePropertyIds.GEAR_SELECTION to CarPropertyManager.SENSOR_RATE_ONCHANGE,
            VehiclePropertyIds.IGNITION_STATE to CarPropertyManager.SENSOR_RATE_ONCHANGE
        )

        properties.forEach { (id, rate) ->
            manager.registerCallback(callback, id, rate)
        }
    }

    private fun processVehicleEvent(value: CarPropertyValue<*>) {
        val currentTime = System.currentTimeMillis()

        when (value.propertyId) {
            VehiclePropertyIds.PERF_VEHICLE_SPEED -> {
                val speed = value.value as Float
                if (lastSpeedTimestamp != 0L) {
                    val deltaTime = (currentTime - lastSpeedTimestamp) / 1000.0
                    if (deltaTime > 0) totalDistanceMeters += (speed * deltaTime)
                }
                lastSpeedTimestamp = currentTime
                _speedFlow.tryEmit(speed)
            }
            VehiclePropertyIds.FUEL_LEVEL -> {
                val fuel = value.value as Float
                if (fuel <= 0f) return

                // 💡 핵심: 연료가 현재 시작점보다 '늘어났다면' 스크립트 재시작이나 주유로 간주하고 리셋
                if (startFuelLevel == null || fuel > startFuelLevel!! + 10f) {
                    startFuelLevel = fuel
                    Log.d("DEBUG_REPO", "📍 연료 시작점 리셋됨: $startFuelLevel")
                }

                latestFuelLevel = fuel
            }
            VehiclePropertyIds.ENGINE_RPM -> {
                _rpmFlow.tryEmit(value.value as Float)
            }
            VehiclePropertyIds.GEAR_SELECTION -> {
                val gear = VehicleGearState.fromInt(value.value as Int)
                val previousGear = _gearFlow.value
                _gearFlow.value = gear

                when (gear) {
                    VehicleGearState.DRIVE -> {
                        // 💡 P에서 D로 바뀔 때만 "새로운 주행"으로 보고 리셋!
                        if (previousGear == VehicleGearState.PARK || !isSessionActive) {
                            resetTrip() // 거리, 연료 시작점, 필터 초기화
                            isSessionActive = true
                            Log.d("CarAPI", "🚀 새로운 주행 세션 시작 (P -> D)")
                        }
                    }
                    VehicleGearState.PARK -> {
                        // 💡 D에서 P로 오면 "일단 주행 종료"로 간주 (시동 OFF 효과)
                        if (isSessionActive) {
                            isSessionActive = false
                            Log.d("CarAPI", "🏁 주행 종료 감지 (D -> P)")
                        }
                    }
                    else -> {
                        // N, R 등은 주행 세션을 유지함 (데이터 리셋 안 함)
                    }
                }
            }
            VehiclePropertyIds.IGNITION_STATE -> {
                val rawValue = value.value as Int
                val state = VehicleIgnitionState.fromInt(rawValue)
                _ignitionFlow.value = state
            }
        }

        // 어떤 데이터가 들어오든 최신 상태로 연비 갱신
        updateEfficiency()
    }

    private fun updateEfficiency() {
        val start = startFuelLevel ?: return
        val current = latestFuelLevel ?: return
        val consumed = start - current
        val distance = totalDistanceMeters

        // 1. [수정] 초기 폭발 방지: 최소 20m 주행 및 10mL 소모 전까지는 0으로 고정
        if (distance < 20.0 || consumed < 10.0f) {
            _fuelEfficiency.value = 0f
            smoothedEfficiency = 0f
            return
        }

        val distanceKm = distance / 1000.0
        val fuelLiters = consumed / 1000.0
        val rawEfficiency = (distanceKm / fuelLiters).toFloat()

        // 2. [수정] 현실적인 캡핑 (현실 세계 연비는 50을 넘기 힘듦)
        val cappedEfficiency = rawEfficiency.coerceIn(0.1f, 50.0f)

        // 3. [수정] 필터 반응 속도 상향 (0.1 -> 0.4)
        // 테스트 시 등급 변화를 더 빠르게 보기 위함
        if (smoothedEfficiency == 0f) {
            // 첫 진입 시 Excellent 방지를 위해 아주 낮은 값부터 시작하게 유도 가능
            // 혹은 계산된 첫 값을 그대로 수용 (이제 안정화 구간 덕분에 60이 안 나옴)
            smoothedEfficiency = cappedEfficiency
        } else {
            smoothedEfficiency += alpha * (cappedEfficiency - smoothedEfficiency)
        }

        _fuelEfficiency.value = smoothedEfficiency
        Log.d("DEBUG_REPO", "📊 Raw: $rawEfficiency, Smoothed: $smoothedEfficiency, Dist: $distance")
    }


    override fun observeSpeed(): Flow<Float> = _speedFlow
    override fun observeFuelLevel(): Flow<Float> = _fuelLevelFlow
    override fun observeEngineRpm(): Flow<Float> = _rpmFlow
    override fun getEfficiency(): Flow<Float> = fuelEfficiency
    override fun getTotalDistance(): Double = totalDistanceMeters

    private fun resetTrip() {
        startFuelLevel = latestFuelLevel
        totalDistanceMeters = 0.0
        lastSpeedTimestamp = 0L
        smoothedEfficiency = 0f
        _fuelEfficiency.value = 0f
        Log.d("DEBUG_REPO", "♻️ Trip Data Reset")
    }
}
package com.example.data.repository

import android.car.Car
import com.example.domain.model.VehicleIgnitionState
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.util.Log
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
    private val _gearFlow = MutableSharedFlow<Int>(replay = 1)
    private val _fuelLevelFlow = MutableSharedFlow<Float>(replay = 1)
    private val _ignitionFlow = MutableStateFlow(VehicleIgnitionState.UNDEFINED)
    override val ignitionState: StateFlow<VehicleIgnitionState> = _ignitionFlow.asStateFlow()

    // --- 연비 계산용 세션 변수 ---
    private var startFuelLevel: Float? = null
    private var latestFuelLevel: Float? = null
    private var totalDistanceMeters: Double = 0.0
    private var lastUpdateTimestamp = 0L

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
                if (lastUpdateTimestamp != 0L) {
                    val deltaTime = (currentTime - lastUpdateTimestamp) / 1000.0
                    if (deltaTime > 0) totalDistanceMeters += (speed * deltaTime)
                }
                lastUpdateTimestamp = currentTime
                _speedFlow.tryEmit(speed)
            }
            VehiclePropertyIds.FUEL_LEVEL -> {
                val fuel = value.value as Float
                if (startFuelLevel == null) startFuelLevel = fuel
                latestFuelLevel = fuel
                _fuelLevelFlow.tryEmit(fuel)
            }
            VehiclePropertyIds.ENGINE_RPM -> {
                _rpmFlow.tryEmit(value.value as Float)
            }
            VehiclePropertyIds.GEAR_SELECTION -> {
                _gearFlow.tryEmit(value.value as Int)
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

        // 1. 연료 소모가 발생했고, 2. 주행 거리가 있을 때만 계산
        if (consumed > 0 && totalDistanceMeters > 0) {
            val distanceKm = totalDistanceMeters / 1000.0
            val fuelLiters = consumed / 1000.0
            val efficiency = (distanceKm / fuelLiters).toFloat()

            if (efficiency in 0f..100f) {
                _fuelEfficiency.value = efficiency
            }
        }
    }

    override fun observeSpeed(): Flow<Float> = _speedFlow
    override fun observeFuelLevel(): Flow<Float> = _fuelLevelFlow
    override fun observeEngineRpm(): Flow<Float> = _rpmFlow
    override fun observeGear(): Flow<Int> = _gearFlow
    override fun getEfficiency(): Flow<Float> = fuelEfficiency
    override fun getTotalDistance(): Double = totalDistanceMeters

    fun resetTrip() {
        startFuelLevel = latestFuelLevel
        totalDistanceMeters = 0.0
        lastUpdateTimestamp = System.currentTimeMillis()
        _fuelEfficiency.value = 0f
    }
}
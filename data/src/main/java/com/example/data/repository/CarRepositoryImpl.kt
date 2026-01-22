package com.example.data.repository

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.util.Log
import com.example.domain.repository.CarRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CarRepository {

    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null

    private var startFuelLevel: Float? = null
    private var totalDistanceMeters: Double = 0.0
    private var lastSpeedTimestamp: Long = 0L


    private val ACCELERATOR_PEDAL_POS = 291504645
    private val BRAKE_PEDAL_ANY_POSITION = 287310337

    init {
        connectToCarService()
    }

    private fun connectToCarService() {
        car = Car.createCar(context, null, Car.CAR_WAIT_TIMEOUT_WAIT_FOREVER) { connectedCar, ready ->
            if (ready) {
                try {
                    carPropertyManager = connectedCar.getCarManager(CarPropertyManager::class.java)

                    val configList = carPropertyManager?.getPropertyList()
                    Log.d("CarAPI", "📊 허용된 속성 개수: ${configList?.size}")
                    configList?.forEach { config ->
                        Log.d("CarAPI", "✅ 허용된 속성: ${config.propertyId} (${config.propertyId == 291504647})")
                    }

                } catch (e: Exception) {
                    Log.e("CarAPI", "Failed to get CarPropertyManager", e)
                }
            }
        }
    }

    private suspend fun waitForManager(): CarPropertyManager {
        while (carPropertyManager == null) {
            kotlinx.coroutines.delay(100)
        }
        return carPropertyManager!!
    }

    // observeSpeed: UseCase가 이 데이터를 받아서 가속도를 계산합니다.
    override fun observeSpeed(): Flow<Float> = callbackFlow {
        val manager = waitForManager()
        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                if (value.propertyId == VehiclePropertyIds.PERF_VEHICLE_SPEED) {
                    val speed = value.value as Float // m/s

                    // 거리 계산 로직
                    val currentTime = System.currentTimeMillis()
                    if (lastSpeedTimestamp != 0L) {
                        val timeDiffSeconds = (currentTime - lastSpeedTimestamp) / 1000.0
                        totalDistanceMeters += speed * timeDiffSeconds
                    }
                    lastSpeedTimestamp = currentTime

                    // 로그 추가: 데이터가 리포지토리에서 나가는지 확인
                    Log.d("CarAPI", "🚀 Speed 전송: $speed m/s")
                    trySend(speed)
                }
            }
            override fun onErrorEvent(propId: Int, zone: Int) {}
        }

        manager.registerCallback(callback, VehiclePropertyIds.PERF_VEHICLE_SPEED, CarPropertyManager.SENSOR_RATE_UI)
        awaitClose { manager.unregisterCallback(callback) }
    }

    override fun observeFuelLevel(): Flow<Float> = callbackFlow {
        val manager = waitForManager()
        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                if (value.propertyId == VehiclePropertyIds.FUEL_LEVEL) {
                    val currentFuel = value.value as Float
                    if (startFuelLevel == null) startFuelLevel = currentFuel
                    Log.d("CarAPI", "⛽ Fuel 전송: $currentFuel")
                    trySend(currentFuel)
                }
            }
            override fun onErrorEvent(propId: Int, zone: Int) {}
        }

        manager.registerCallback(callback, VehiclePropertyIds.FUEL_LEVEL, CarPropertyManager.SENSOR_RATE_NORMAL)
        awaitClose { manager.unregisterCallback(callback) }
    }

    override fun getEfficiency(): Flow<Float> = callbackFlow {
        val manager = waitForManager()
        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                val currentFuel = if (value.propertyId == VehiclePropertyIds.FUEL_LEVEL) value.value as Float else null

                if (startFuelLevel != null && totalDistanceMeters > 0) {
                    val fuelConsumedMl = startFuelLevel!! - (currentFuel ?: startFuelLevel!!)
                    if (fuelConsumedMl > 0) {
                        val distanceKm = totalDistanceMeters / 1000.0
                        val fuelLiters = fuelConsumedMl / 1000.0
                        val efficiency = (distanceKm / fuelLiters).toFloat()
                        trySend(efficiency)
                    } else {
                        trySend(0f)
                    }
                }
            }
            override fun onErrorEvent(propId: Int, zone: Int) {}
        }

        manager.registerCallback(callback, VehiclePropertyIds.PERF_VEHICLE_SPEED, CarPropertyManager.SENSOR_RATE_UI)
        manager.registerCallback(callback, VehiclePropertyIds.FUEL_LEVEL, CarPropertyManager.SENSOR_RATE_NORMAL)
        awaitClose { manager.unregisterCallback(callback) }
    }

    override fun observeEngineRpm(): Flow<Float> = callbackFlow {
        val manager = waitForManager()
        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                if (value.propertyId == VehiclePropertyIds.ENGINE_RPM) {
                    trySend(value.value as Float)
                }
            }
            override fun onErrorEvent(p0: Int, p1: Int) {}
        }
        manager.registerCallback(callback, VehiclePropertyIds.ENGINE_RPM, CarPropertyManager.SENSOR_RATE_UI)
        awaitClose { manager.unregisterCallback(callback) }
    }

    override fun observeGear(): Flow<Int> = callbackFlow {
        val manager = waitForManager()
        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                if (value.propertyId == VehiclePropertyIds.GEAR_SELECTION) {
                    trySend(value.value as Int)
                }
            }
            override fun onErrorEvent(p0: Int, p1: Int) {}
        }
        manager.registerCallback(callback, VehiclePropertyIds.GEAR_SELECTION, CarPropertyManager.SENSOR_RATE_ONCHANGE)
        awaitClose { manager.unregisterCallback(callback) }
    }

}
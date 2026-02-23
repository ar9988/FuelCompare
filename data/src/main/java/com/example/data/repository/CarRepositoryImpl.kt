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

    companion object {
        private const val EMULATOR_NOISE_SPEED = 9000f
        private const val EMULATOR_NOISE_FUEL = 90000f
        private const val EMULATOR_NOISE_RPM = 9000f

        //  엔진 RPM 대신 외부 온도 센서 ID를 가짜 RPM으로 사용
        private const val DUMMY_RPM_ID = VehiclePropertyIds.ENV_OUTSIDE_TEMPERATURE
    }

    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null

    // --- 데이터 스트림 ---
    private val _speedFlow = MutableSharedFlow<Float>(replay = 1)
    private val _rpmFlow = MutableSharedFlow<Float>(replay = 1)
    private val _gearFlow = MutableStateFlow(VehicleGearState.UNDEFINED)
    private val _fuelLevelFlow = MutableSharedFlow<Float>(replay = 1)
    private val _ignitionFlow = MutableStateFlow(VehicleIgnitionState.UNDEFINED)

    override val gearState: StateFlow<VehicleGearState> = _gearFlow.asStateFlow()
    override val ignitionState: StateFlow<VehicleIgnitionState> = _ignitionFlow.asStateFlow()

    init {
        connectToCarService()
    }

    private fun connectToCarService() {
        car = Car.createCar(context, null, Car.CAR_WAIT_TIMEOUT_WAIT_FOREVER) { connectedCar, ready ->
            if (ready) {
                try {
                    carPropertyManager = connectedCar.getCarManager(CarPropertyManager::class.java)
                    registerAllCallbacks()
                    fetchInitialValues()
                } catch (e: Exception) {
                    Log.e("CarAPI_DEBUG", "❌ 매니저 획득 실패", e)
                }
            }
        }
    }

    private fun registerAllCallbacks() {
        val manager = carPropertyManager ?: return
        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                // 🔍 [모든 신호 수신 로그] 어떤 ID와 값이 들어오는지 무조건 찍음
                Log.d("CarAPI_DEBUG", "📥 Received Raw -> ID: ${value.propertyId}, Value: ${value.value}")

                when (value.propertyId) {
                    VehiclePropertyIds.PERF_VEHICLE_SPEED -> {
                        val speed = value.value as? Float ?: 0f
                        if (speed >= EMULATOR_NOISE_SPEED) {
                            // 노이즈 차단
                        } else {
                            _speedFlow.tryEmit(speed)
                            Log.v("CarAPI_DEBUG", "   ㄴ [SPEED] Valid: $speed")
                        }
                    }

                    VehiclePropertyIds.FUEL_LEVEL -> {
                        val fuel = value.value as? Float ?: 0f
                        if (fuel >= EMULATOR_NOISE_FUEL || fuel <= 0f) {
                            // 노이즈 차단
                        } else {
                            _fuelLevelFlow.tryEmit(fuel)
                            Log.v("CarAPI_DEBUG", "   ㄴ [FUEL] Valid: $fuel")
                        }
                    }

                    // ENGINE_RPM 대신 DUMMY_RPM_ID(외부 온도)가 들어오면 RPM으로 취급
                    DUMMY_RPM_ID -> {
                        val rpm = value.value as? Float ?: 0f
                        if (rpm >= EMULATOR_NOISE_RPM) {
                            Log.w("CarAPI_DEBUG", "   ㄴ [RPM(온도센서)] 🚫 에뮬레이터 노이즈($rpm) 무시")
                        } else {
                            Log.v("CarAPI_DEBUG", "   ㄴ [RPM(온도센서)] Valid: $rpm")
                            _rpmFlow.tryEmit(rpm)
                        }
                    }

                    VehiclePropertyIds.GEAR_SELECTION -> {
                        val rawGear = value.value as? Int ?: 0
                        val gearState = VehicleGearState.fromInt(rawGear)
                        Log.i("CarAPI_DEBUG", "   ㄴ [GEAR] Raw: $rawGear -> State: $gearState")
                        _gearFlow.value = gearState
                    }

                    VehiclePropertyIds.IGNITION_STATE -> {
                        val rawIgnition = value.value as? Int ?: 0
                        val ignitionState = VehicleIgnitionState.fromInt(rawIgnition)
                        Log.i("CarAPI_DEBUG", "   ㄴ [IGNITION] Raw: $rawIgnition -> State: $ignitionState")
                        _ignitionFlow.value = ignitionState
                    }
                }
            }
            override fun onErrorEvent(propId: Int, zone: Int) {
                Log.e("CarAPI_DEBUG", "❌ Property Error: ID $propId in zone $zone")
            }
        }

        val properties = listOf(
            VehiclePropertyIds.PERF_VEHICLE_SPEED to CarPropertyManager.SENSOR_RATE_UI,
            VehiclePropertyIds.FUEL_LEVEL to CarPropertyManager.SENSOR_RATE_NORMAL,
            DUMMY_RPM_ID to CarPropertyManager.SENSOR_RATE_UI,
            VehiclePropertyIds.GEAR_SELECTION to CarPropertyManager.SENSOR_RATE_ONCHANGE,
            VehiclePropertyIds.IGNITION_STATE to CarPropertyManager.SENSOR_RATE_ONCHANGE
        )

        Log.d("CarAPI_DEBUG", "⚙️ Registering Callbacks for ${properties.size} properties...")
        properties.forEach { (id, rate) -> manager.registerCallback(callback, id, rate) }
    }

    private fun fetchInitialValues() {
        val manager = carPropertyManager ?: return
        try {
            val gear = manager.getProperty<Int>(VehiclePropertyIds.GEAR_SELECTION, 0)
            _gearFlow.value = VehicleGearState.fromInt(gear.value)
            Log.d("CarAPI_DEBUG", "🏁 Initial GEAR: ${_gearFlow.value}")

            val ignition = manager.getProperty<Int>(VehiclePropertyIds.IGNITION_STATE, 0)
            _ignitionFlow.value = VehicleIgnitionState.fromInt(ignition.value)
            Log.d("CarAPI_DEBUG", "🔑 Initial IGNITION: ${_ignitionFlow.value}")
        } catch (e: Exception) {
            Log.w("CarAPI_DEBUG", "⚠️ 초기 데이터 로드 실패 (지원되지 않을 수 있음)")
        }
    }

    fun shutdown() {
        Log.d("CarAPI_DEBUG", "🛑 Shutting down CarRepository")
        carPropertyManager?.unregisterCallback(null)
        car?.disconnect()
        car = null
    }

    override fun observeSpeed(): Flow<Float> = _speedFlow
    override fun observeFuelLevel(): Flow<Float> = _fuelLevelFlow
    override fun observeEngineRpm(): Flow<Float> = _rpmFlow
}
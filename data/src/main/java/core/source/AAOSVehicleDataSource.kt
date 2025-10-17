package core.source

import android.car.Car
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.car.VehiclePropertyIds
import android.content.Context
import core.model.VehicleDataDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // 앱 전체에서 단 하나의 인스턴스만 유지하도록 설정
class AAOSVehicleDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val car: Car = Car.createCar(context)
    private val propertyManager: CarPropertyManager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager

    // 관찰할 차량 속성 ID 목록
    private val propertiesToObserve = listOf(
        VehiclePropertyIds.EV_BATTERY_LEVEL,
        VehiclePropertyIds.PERF_ODOMETER
    )

    fun observeVehicleStatus(): Flow<VehicleDataDto> = callbackFlow {
        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                // 속성 값이 변경될 때마다, 필요한 모든 속성을 다시 읽어 완전한 데이터 세트를 만듭니다.
                val batteryLevel = propertyManager.getFloatProperty(VehiclePropertyIds.EV_BATTERY_LEVEL, 0)
                val odometer = propertyManager.getFloatProperty(VehiclePropertyIds.PERF_ODOMETER, 0)

                // 두 값이 모두 유효할 때만 DTO를 생성하고 Flow로 발행합니다.
                if (batteryLevel != null && odometer != null) {
                    trySend(
                        VehicleDataDto(
                            totalMileageKm = odometer,
                            remainingBatteryPercent = batteryLevel
                        )
                    )
                }
            }

            override fun onErrorEvent(propId: Int, zone: Int) {
                close(Exception("AAOS Property Error occurred for ID: $propId"))
            }
        }

        // Flow 구독이 시작되면, 각 속성에 대해 콜백을 등록합니다.
        propertiesToObserve.forEach { propId ->
            propertyManager.registerCallback(callback, propId, CarPropertyManager.SENSOR_RATE_ONCHANGE)
        }

        // Flow 구독이 취소되면, 등록했던 콜백을 해제하여 메모리 누수를 방지합니다.
        awaitClose {
            propertyManager.unregisterCallback(callback)
        }
    }

    // 앱이 종료될 때 Car 서비스와의 연결을 해제하기 위한 함수
    fun release() {
        car.disconnect()
    }
}
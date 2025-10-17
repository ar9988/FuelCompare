package com.example.domain.usecase

import com.example.domain.model.VehicleStatus
import com.example.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import javax.inject.Inject

// Pair<이전 상태?, 현재 상태>를 위한 타입 별칭
private typealias StatusPair = Pair<VehicleStatus?, VehicleStatus>

/**
 * 차량의 상태 변화를 관찰하여 실시간 연비를 계산하는 유스케이스.
 *
 * @param homeRepository 데이터 소스로부터 차량 상태를 가져오기 위한 Repository
 * @param totalBatteryCapacityKwh 차량의 총 배터리 용량 (kWh). 정확한 연비 계산을 위해 필요.
 */
class ObserveFuelEfficiencyUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    // TODO: 실제 차량의 총 배터리 용량을 상수로 정의해야 합니다. (예: 77.4 kWh)
    private val totalBatteryCapacityKwh = 77.4

    /**
     * 유스케이스를 실행하여 실시간 연비(km/kWh) 스트림을 반환한다.
     */
    operator fun invoke(): Flow<Double> {
        return homeRepository.observeVehicleStatus()
            // 1. scan: 이전 VehicleStatus 값을 기억하여 현재 값과 함께 Pair로 묶어준다.
            //    초기값은 null to VehicleStatus(...)로 설정하여 첫 번째 데이터를 처리할 수 있게 한다.
            .scan<VehicleStatus, StatusPair>(initial = null to VehicleStatus(0.0, 0.0)) { accumulator, current ->
                accumulator.second to current // 이전의 '현재' 값을 다음의 '이전' 값으로 넘김
            }
            // 2. map: Pair를 사용하여 이전-현재 값의 변화량으로 연비를 계산한다.
            .map { (previous, current) ->
                // 첫 데이터는 이전 값이 없으므로 계산에서 제외
                if (previous?.totalMileage == 0.0 || previous == null) {
                    return@map 0.0
                }

                val distanceDelta = current.totalMileage - previous.totalMileage // 주행 거리 변화량 (km)

                // 배터리 % 변화량을 실제 에너지(kWh) 소모량으로 변환
                val batteryPercentDelta = previous.remainingBattery - current.remainingBattery
                val energyConsumedKwh = (batteryPercentDelta / 100.0) * totalBatteryCapacityKwh

                // 분모가 0이 되는 경우를 방지
                if (energyConsumedKwh <= 0) {
                    return@map 0.0
                }

                // 최종 연비 계산 (km/kWh)
                val efficiency = distanceDelta / energyConsumedKwh
                efficiency
            }
    }
}
package core.mapper

import com.example.domain.model.VehicleStatus
import core.model.VehicleDataDto

// DTO를 Domain Model로 변환
fun VehicleDataDto.toDomain(): VehicleStatus {
    return VehicleStatus(
        totalMileage = this.totalMileageKm.toDouble(),
        // 참고: Domain Model의 remainingBattery가 % 단위라고 가정합니다.
        // 만약 kWh 단위가 필요하다면, 여기서 차량의 총 배터리 용량(상수)을 곱하는 변환 로직이 추가되어야 합니다.
        remainingBattery = this.remainingBatteryPercent.toDouble()
    )
}
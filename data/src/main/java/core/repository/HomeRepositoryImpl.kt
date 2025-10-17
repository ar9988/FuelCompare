package core.repository

import com.example.domain.repository.HomeRepository
import core.datasource.AAOSVehicleDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import com.example.domain.model.VehicleStatus
import core.mapper.toDomain

@Singleton
class HomeRepositoryImpl @Inject constructor(
    private val vehicleDataSource: AAOSVehicleDataSource
) : HomeRepository {
    override fun observeVehicleStatus(): Flow<VehicleStatus> {
        // DataSource로부터 DTO 스트림을 받아
        return vehicleDataSource.observeVehicleStatus()
            // 각 DTO를 Domain Model로 변환하여 상위 계층으로 전달합니다.
            .map { dto ->
                dto.toDomain()
            }
    }
}
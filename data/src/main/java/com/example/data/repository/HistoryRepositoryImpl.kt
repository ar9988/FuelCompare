package com.example.data.repository

import com.example.data.dao.TripHistoryDao
import com.example.data.entity.TripHistoryEntity
import com.example.domain.model.TripHistory
import com.example.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val tripHistoryDao: TripHistoryDao
) : HistoryRepository {

    override suspend fun saveTrip(history: TripHistory) {
        // Domain -> Entity 변환 후 저장
        val entity = TripHistoryEntity(
            date = history.date,
            avgEfficiency = history.avgEfficiency,
            harshAccelCount = history.harshAccelCount,
            harshBrakeCount = history.harshBrakeCount,
            totalDistanceMeter = history.totalDistance
        )
        tripHistoryDao.insertTrip(entity)
    }

    override fun getAllHistory(): Flow<List<TripHistory>> {
        return tripHistoryDao.getAllTrips().map { entities ->
            entities.map {
                TripHistory(
                    id = it.id,
                    date = it.date,
                    avgEfficiency = it.avgEfficiency,
                    harshAccelCount = it.harshAccelCount,
                    harshBrakeCount = it.harshBrakeCount,
                    totalDistance = it.totalDistanceMeter
                )
            }
        }
    }
}
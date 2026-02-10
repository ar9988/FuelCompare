package com.example.data.repository

import android.util.Log
import com.example.data.dao.TripHistoryDao
import com.example.data.entity.TripHistoryEntity
import com.example.domain.model.TripHistory
import com.example.domain.model.TripStatistics
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
        Log.d("History","save trip $history")
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

    override fun getMonthlyStats(): Flow<List<TripStatistics>> {
        return tripHistoryDao.getMonthlyStatistics().map { entities ->
            entities.map {
                TripStatistics(
                    timestamp = it.representativeTimestamp,
                    avgEfficiency = it.avgEfficiency,
                    harshAccelCount = it.harshAccelCount,
                    harshBrakeCount = it.harshBrakeCount,
                    totalDistance = it.totalDistance
                )
            }
        }
    }

    override fun getDailyStats(): Flow<List<TripStatistics>> {
        return tripHistoryDao.getDailyStatistics().map { entities ->
            entities.map {
                TripStatistics(
                    timestamp = it.representativeTimestamp,
                    avgEfficiency = it.avgEfficiency,
                    harshAccelCount = it.harshAccelCount,
                    harshBrakeCount = it.harshBrakeCount,
                    totalDistance = it.totalDistance
                )
            }
        }
    }

    override suspend fun seedDummyData() {
        val dao = tripHistoryDao // 이미 주입된 DAO 사용
        val calendar = java.util.Calendar.getInstance()

        val dummyList = mutableListOf<TripHistoryEntity>()

        // 최근 40일치 데이터를 역순으로 생성
        for (i in 0..600) {
            val tempCal = calendar.clone() as java.util.Calendar
            tempCal.add(java.util.Calendar.DAY_OF_YEAR, -i)

            // 랜덤 데이터 생성
            val randomEfficiency = (10..22).random().toFloat() + (0..9).random() / 10f
            val randomDistance = (5000..50000).random().toDouble() // 5km ~ 50km
            val randomAccel = (0..5).random()
            val randomBrake = (0..5).random()

            dummyList.add(
                TripHistoryEntity(
                    date = tempCal.timeInMillis,
                    avgEfficiency = randomEfficiency,
                    harshAccelCount = randomAccel,
                    harshBrakeCount = randomBrake,
                    totalDistanceMeter = randomDistance
                )
            )
        }

        // DB에 일괄 삽입
        dummyList.forEach { dao.insertTrip(it) }
    }
}
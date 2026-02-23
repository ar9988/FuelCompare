package com.example.data.repository

import android.util.Log
import com.example.data.dao.TripHistoryDao
import com.example.data.entity.TripHistoryEntity
import com.example.domain.model.DrivingHabitSummary
import com.example.domain.model.TripHistory
import com.example.domain.model.TripStatistics
import com.example.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

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
            totalDistanceMeter = history.totalDistance,
            coastingCount = history.coastingCount,
            cruiseCount = history.cruiseCount,
            idlingTimeMillis = history.idlingTimeMillis,
            coastingTimeMillis = history.coastingTimeMillis,
            cruiseTimeMillis = history.cruiseTimeMillis,
            idlingCount = history.idlingCount,
            totalDurationMillis = history.tripDuration
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
                    totalDistance = it.totalDistanceMeter,
                    coastingCount = it.coastingCount,
                    cruiseCount = it.cruiseCount,
                    idlingTimeMillis = it.idlingTimeMillis,
                    coastingTimeMillis = it.coastingTimeMillis,
                    cruiseTimeMillis = it.cruiseTimeMillis,
                    idlingCount = it.idlingCount,
                    tripDuration = it.totalDurationMillis,
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
                    totalDistance = it.totalDistance,
                    coastingCount = it.coastingCount,
                    cruiseCount = it.cruiseCount,
                    idlingTimeMillis = it.idlingTimeMillis,
                    coastingTimeMillis = it.coastingTimeMillis,
                    cruiseTimeMillis = it.cruiseTimeMillis,
                    idlingCount = it.idlingCount,
                    tripDuration = it.totalTripTime
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
                    totalDistance = it.totalDistance,
                    coastingCount = it.coastingCount,
                    cruiseCount = it.cruiseCount,
                    idlingTimeMillis = it.idlingTimeMillis,
                    coastingTimeMillis = it.coastingTimeMillis,
                    cruiseTimeMillis = it.cruiseTimeMillis,
                    idlingCount = it.idlingCount,
                    tripDuration = it.totalTripTime
                )
            }
        }
    }

    override suspend fun getHistorySince(sevenDaysAgo: Long): DrivingHabitSummary {
        val tmp = tripHistoryDao.getHistorySince(sevenDaysAgo)
        return DrivingHabitSummary(
            avgEfficiency = tmp?.averageEfficiency ?: 0f,
            harshAccelCount = tmp?.totalAccelCount ?: 0,
            harshBrakeCount = tmp?.totalBrakeCount ?: 0,
            totalDistance = tmp?.totalDistance ?: 0.0,
            coastingCount = tmp?.totalCoastingCount ?: 0,
            cruiseCount = tmp?.totalCruiseCount ?: 0,
            idlingCount = tmp?.totalIdlingCount ?: 0,
            idlingTimeMillis = tmp?.totalIdlingTime ?: 0L,
            coastingTimeMillis = tmp?.totalCoastingTime ?: 0L,
            cruiseTimeMillis = tmp?.totalCruiseTime ?: 0L,
            tripCount = tmp?.tripCount ?: 0,
            totalDurationMillis = tmp?.totalTripTime ?: 0L
        )
    }

    override suspend fun seedDummyData() {
        val dao = tripHistoryDao
        val calendar = Calendar.getInstance()
        val dummyList = mutableListOf<TripHistoryEntity>()

        for (i in 0..600) {
            val tempCal = calendar.clone() as Calendar
            tempCal.add(Calendar.DAY_OF_YEAR, -i)

            // 1. 주행 거리 결정 (5km ~ 50km)
            val randomDistanceMeter = Random.nextDouble(5000.0, 50000.0)

            // 2. 평균 속도를 가정하여 주행 시간 계산 (8m/s ~ 16m/s)
            val averageSpeedMps = Random.nextDouble(8.0, 16.0)
            val tripDurationSeconds = (randomDistanceMeter / averageSpeedMps).toLong()
            val totalDurationMillis = tripDurationSeconds * 1000

            // 3. 주행 시간에 비례한 상태별 시간 배분 (Random.nextDouble 사용)
            val idlingTime = (totalDurationMillis * Random.nextDouble(0.05, 0.15)).toLong()
            val coastingTime = (totalDurationMillis * Random.nextDouble(0.1, 0.2)).toLong()
            val cruiseTime = (totalDurationMillis * Random.nextDouble(0.2, 0.4)).toLong()

            // 4. 연비 결정 (경제 운전 비중 반영)
            val baseEfficiency = Random.nextDouble(12.0, 16.0).toFloat()
            val ecoBonus = ((coastingTime + cruiseTime).toFloat() / totalDurationMillis) * 10f
            val randomEfficiency = baseEfficiency + ecoBonus

            dummyList.add(
                TripHistoryEntity(
                    date = tempCal.timeInMillis,
                    avgEfficiency = randomEfficiency,
                    harshAccelCount = (0..3).random(),
                    harshBrakeCount = (0..2).random(),
                    totalDistanceMeter = randomDistanceMeter,
                    coastingCount = (1..5).random(),
                    cruiseCount = (1..8).random(),
                    idlingCount = (1..4).random(),
                    idlingTimeMillis = idlingTime,
                    coastingTimeMillis = coastingTime,
                    cruiseTimeMillis = cruiseTime,
                    totalDurationMillis = totalDurationMillis
                )
            )
        }
        // 일괄 삽입
        dao.insertAll(dummyList)
    }
}
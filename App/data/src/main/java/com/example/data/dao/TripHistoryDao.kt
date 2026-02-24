package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.data.entity.DrivingHabitSummaryEntity
import com.example.data.entity.TripHistoryEntity
import com.example.data.entity.TripStatisticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripHistoryDao {
    @Insert
    suspend fun insertTrip(trip: TripHistoryEntity)

    @Query("SELECT * FROM trip_history ORDER BY date DESC")
    fun getAllTrips(): Flow<List<TripHistoryEntity>>

    @Query("""
        SELECT 
            MIN(date) as representativeTimestamp, 
            AVG(avgEfficiency) as avgEfficiency,
            SUM(harshAccelCount) as harshAccelCount,
            SUM(harshBrakeCount) as harshBrakeCount,
            SUM(totalDistanceMeter) as totalDistance,
            SUM(coastingCount) as coastingCount,
            SUM(cruiseCount) as cruiseCount,
            SUM(idlingCount) as idlingCount,
            SUM(idlingTimeMillis) as idlingTimeMillis,
            SUM(coastingTimeMillis) as coastingTimeMillis,
            SUM(cruiseTimeMillis) as cruiseTimeMillis,
            SUM(totalDurationMillis)as totalTripTime
        FROM trip_history 
        GROUP BY strftime('%Y-%m-%d', date / 1000, 'unixepoch', 'localtime') 
        ORDER BY representativeTimestamp ASC
    """)
    fun getDailyStatistics(): Flow<List<TripStatisticsEntity>>

    // 월별: 월별로 그룹화
    @Query("""
        SELECT 
            MIN(date) as representativeTimestamp, 
            AVG(avgEfficiency) as avgEfficiency,
            SUM(harshAccelCount) as harshAccelCount,
            SUM(harshBrakeCount) as harshBrakeCount,
            SUM(totalDistanceMeter) as totalDistance,
            SUM(coastingCount) as coastingCount,
            SUM(cruiseCount) as cruiseCount,
            SUM(idlingCount) as idlingCount,
            SUM(idlingTimeMillis) as idlingTimeMillis,
            SUM(coastingTimeMillis) as coastingTimeMillis,
            SUM(cruiseTimeMillis) as cruiseTimeMillis,
            SUM(totalDurationMillis)as totalTripTime
        FROM trip_history 
        GROUP BY strftime('%Y-%m', date / 1000, 'unixepoch', 'localtime') 
        ORDER BY representativeTimestamp ASC
    """)
    fun getMonthlyStatistics(): Flow<List<TripStatisticsEntity>>


    @Query("""
        SELECT 
            SUM(harshAccelCount) as totalAccelCount,
            SUM(harshBrakeCount) as totalBrakeCount,
            SUM(coastingCount) as totalCoastingCount,
            SUM(cruiseCount) as totalCruiseCount,
            SUM(idlingTimeMillis) as totalIdlingTime,
            SUM(totalDistanceMeter) as totalDistance,
            AVG(avgEfficiency) as averageEfficiency,
            SUM(idlingCount) as totalIdlingCount,
            SUM(cruiseTimeMillis) as totalCruiseTime,
            SUM(coastingTimeMillis) as totalCoastingTime,
            COUNT(*) as tripCount,
            SUM(totalDurationMillis) as totalTripTime
        FROM trip_history 
        WHERE date >= :startTime
    """)
    suspend fun getHistorySince(startTime: Long): DrivingHabitSummaryEntity?

    @Insert
    suspend fun insertAll(dummyList: MutableList<TripHistoryEntity>)
}
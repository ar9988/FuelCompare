package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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
            SUM(totalDistanceMeter) as totalDistance
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
            SUM(totalDistanceMeter) as totalDistance
        FROM trip_history 
        GROUP BY strftime('%Y-%m', date / 1000, 'unixepoch', 'localtime') 
        ORDER BY representativeTimestamp ASC
    """)
    fun getMonthlyStatistics(): Flow<List<TripStatisticsEntity>>
}
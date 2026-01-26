package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.data.entity.TripHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripHistoryDao {
    @Insert
    suspend fun insertTrip(trip: TripHistoryEntity)

    @Query("SELECT * FROM trip_history ORDER BY date DESC")
    fun getAllTrips(): Flow<List<TripHistoryEntity>>
}
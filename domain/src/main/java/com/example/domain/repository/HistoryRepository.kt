package com.example.domain.repository

import com.example.domain.model.TripHistory
import com.example.domain.model.TripStatistics
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun seedDummyData()
    suspend fun saveTrip(history: TripHistory)
    fun getAllHistory(): Flow<List<TripHistory>>
    fun getMonthlyStats(): Flow<List<TripStatistics>>
    fun getDailyStats(): Flow<List<TripStatistics>>
}
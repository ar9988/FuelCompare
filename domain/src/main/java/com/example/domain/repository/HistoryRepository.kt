package com.example.domain.repository

import com.example.domain.model.TripHistory
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun saveTrip(history: TripHistory)
    fun getAllHistory(): Flow<List<TripHistory>>
}
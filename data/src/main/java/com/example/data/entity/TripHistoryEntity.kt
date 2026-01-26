package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_history")
data class TripHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val avgEfficiency: Float,
    val harshAccelCount: Int,
    val harshBrakeCount: Int,
    val totalDistanceMeter: Double
)
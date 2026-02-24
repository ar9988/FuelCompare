package com.example.domain.model

data class TripStatistics(
    val timestamp: Long,
    val avgEfficiency: Float,
    val harshAccelCount: Int,
    val harshBrakeCount: Int,
    val totalDistance: Double,
    val coastingCount:Int,
    val cruiseCount:Int,
    val idlingCount: Int,
    val idlingTimeMillis: Long,
    val coastingTimeMillis: Long,
    val cruiseTimeMillis: Long,
    val tripDuration: Long
)
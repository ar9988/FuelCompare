package com.example.domain.model

data class TripHistory(
    val id: Long = 0,
    val date: Long,
    val avgEfficiency: Float,
    val harshAccelCount: Int,
    val harshBrakeCount: Int,
    val totalDistance: Double,
    val coastingCount: Int,
    val cruiseCount: Int,
    val coastingTimeMillis: Long,
    val cruiseTimeMillis: Long,
    val idlingCount: Int,
    val idlingTimeMillis: Long,
    val tripDuration:Long
)
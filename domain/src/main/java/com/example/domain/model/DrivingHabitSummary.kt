package com.example.domain.model

data class DrivingHabitSummary(
    val avgEfficiency: Float,
    val harshAccelCount: Int,
    val harshBrakeCount: Int,
    val totalDistance: Double,
    val idlingCount: Int,
    val coastingCount:Int,
    val cruiseCount:Int,
    val idlingTimeMillis: Long,
    val coastingTimeMillis: Long,
    val cruiseTimeMillis: Long,
    val tripCount:Int,
    val totalDurationMillis: Long
)
package com.example.domain.model

data class TripStatistics(
    val timestamp: Long,
    val avgEfficiency: Float,
    val harshAccelCount: Int,
    val harshBrakeCount: Int,
    val totalDistance: Double
)
package com.example.data.entity

data class DrivingHabitSummaryEntity(
    val totalAccelCount:Int,
    val totalBrakeCount:Int,
    val totalCoastingCount:Int,
    val totalCruiseCount:Int,
    val totalIdlingTime:Long,
    val totalDistance:Double,
    val averageEfficiency:Float,
    val totalIdlingCount:Int,
    val totalCruiseTime:Long,
    val totalCoastingTime:Long,
    val tripCount:Int,
    val totalTripTime:Long
)
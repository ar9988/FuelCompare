package com.example.data.entity

data class TripStatisticsEntity(
    val representativeTimestamp: Long, // 해당 날짜/월의 대표 시간값
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
    val totalTripTime: Long,
)
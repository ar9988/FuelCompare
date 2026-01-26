package com.example.domain.model

data class TripHistory(
    val id: Long = 0,
    val date: Long,           // 주행 날짜
    val avgEfficiency: Float, // 최종 연비
    val harshAccelCount: Int, // 급가속 횟수
    val harshBrakeCount: Int, // 급감속 횟수
    val totalDistance: Double // 총 주행 거리
)
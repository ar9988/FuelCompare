package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_history")
data class TripHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,                  // 주행 종료 시점 (System.currentTimeMillis)
    val avgEfficiency: Float,        // 평균 연비 (km/L)
    val harshAccelCount: Int,        // 급가속 횟수
    val harshBrakeCount: Int,        // 급감속 횟수
    val totalDistanceMeter: Double,  // 총 주행 거리 (m)
    val coastingCount: Int,          // 타력 주행 횟수
    val cruiseCount: Int,            // 정속 주행 횟수
    val coastingTimeMillis: Long,    // 타력 주행 합계 시간 (ms)
    val cruiseTimeMillis: Long,      // 정속 주행 합계 시간 (ms)
    val idlingCount: Int,            // 공회전 횟수 (시동 켜고 멈춘 횟수)
    val idlingTimeMillis: Long,       // 공회전 합계 시간 (ms)
    val totalDurationMillis: Long,   // 전체 주행 시간
)
package com.example.fuelcompare.presentation.detail

data class DetailState(
    val selectedPeriod: DisplayPeriod = DisplayPeriod.DAILY,
    val isLoading: Boolean = false,
    val chartData: List<ChartPoint> = emptyList(), // 30일치 혹은 12개월치 고정 리스트
    val selectedPoint: ChartPoint? = null,        // 클릭된 점의 데이터
    val errorMessage: String? = null
)

enum class DisplayPeriod(
    val threshold:Int
) { DAILY(5), MONTHLY(50) }

data class ChartPoint(
    val dateMillis: Long,
    val avgEfficiency: Float?,// 데이터가 없으면 null
    val harshAccelCount: Int = 0,
    val harshBrakeCount: Int = 0,
    val totalDistance: Double = 0.0
)

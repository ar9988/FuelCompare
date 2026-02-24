package com.example.domain.model

enum class HabitType { HARSH_ACCEL, HARSH_BRAKE, LOW_COASTING, GOOD_COASTING, HIGH_IDLING, GOOD_CRUISE, NORMAL_INFO }
enum class TipType { GENTLE_START, STEADY_SPEED, STOP_IDLING, COASTING_MORE }

data class HabitAnalysisResult(
    val summaries: List<HabitType>,
    val recommendations: List<TipType>,
    val rawSummary: DrivingHabitSummary
)
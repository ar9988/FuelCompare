package com.example.domain.usecase

import com.example.domain.model.HabitAnalysisResult
import com.example.domain.model.HabitType
import com.example.domain.model.TipType
import javax.inject.Inject

class AnalyzeDrivingHabitsUseCase @Inject constructor(
    private val getSummaryUseCase: GetDrivingHabitSummaryUseCase
) {
    suspend operator fun invoke(): HabitAnalysisResult {
        val summary = getSummaryUseCase()

        val summaryList = mutableListOf<HabitType>()
        val tipList = mutableListOf<TipType>()

        // --- 1. 요약 카드 선정 (가장 심각한 것 vs 가장 칭찬할 것 하나씩) ---
        // 나쁜 습관 (우선순위: 급가속 > 급감속)
        if (summary.harshAccelCount >= 5) summaryList.add(HabitType.HARSH_ACCEL)
        else if (summary.harshBrakeCount >= 5) summaryList.add(HabitType.HARSH_BRAKE)

        // 좋은 습관 (우선순위: 타력주행 > 정속주행)
        if (summary.coastingCount >= 10) summaryList.add(HabitType.GOOD_COASTING)
        else if (summary.cruiseCount >= 10) summaryList.add(HabitType.GOOD_CRUISE)

        // 데이터가 부족할 경우를 대비한 기본값 채우기 (최대 2개 유지)
        if (summaryList.size < 2) summaryList.add(HabitType.NORMAL_INFO)

        // --- 2. 추천 팁 선정 (중요도 순으로 정렬 후 상위 3개만) ---
        if (summary.harshAccelCount > 0) tipList.add(TipType.GENTLE_START)
        if (summary.idlingTimeMillis > 0L) tipList.add(TipType.STOP_IDLING)
        if (summary.cruiseCount > 0) tipList.add(TipType.STEADY_SPEED)
        if (summary.coastingCount < 500) tipList.add(TipType.COASTING_MORE)

        // 최대 3개까지만 노출하도록 제한
        return HabitAnalysisResult(
            summaries = summaryList.take(2),
            recommendations = tipList.take(5),
            rawSummary = summary
        )
    }
}
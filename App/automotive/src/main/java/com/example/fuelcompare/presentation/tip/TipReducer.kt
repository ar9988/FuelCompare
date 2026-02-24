package com.example.fuelcompare.presentation.tip

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Speed
import com.example.domain.model.DrivingHabitSummary
import com.example.domain.model.HabitType
import com.example.domain.model.TipType
import com.example.domain.util.ResourceProvider
import com.example.fuelcompare.R
import javax.inject.Inject

class TipReducer @Inject constructor(
    private val res: ResourceProvider
) {
    fun reduce(state: TipState, event: TipEvent): TipState {
        when(event){
            is TipEvent.DataLoaded -> {
                val result = event.result
                return state.copy(
                    isLoading = false,
                    summaries = result.summaries.map { mapToSummary(it, result.rawSummary) },
                    recommendations = result.recommendations.map { mapToTip(it, result.rawSummary) }
                )
            }
            is TipEvent.Error -> {
                return state
            }
            TipEvent.LoadData -> return state.copy(
                isLoading = true
            )
        }
    }

    private fun mapToSummary(type: HabitType, raw: DrivingHabitSummary): SummaryUiModel = when(type) {
        HabitType.HARSH_ACCEL -> SummaryUiModel(
            title = res.getString(R.string.habit_harsh_acceleration_title, raw.harshAccelCount),
            description = res.getString(R.string.habit_harsh_acceleration_desc_bad),
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            color = CardHighlightType.ALERT,
        )
        HabitType.HARSH_BRAKE -> SummaryUiModel(
            title = res.getString(R.string.habit_harsh_brake_title, raw.harshBrakeCount),
            description = res.getString(R.string.habit_harsh_brake_desc),
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            color = CardHighlightType.ALERT,
        )
        HabitType.LOW_COASTING -> SummaryUiModel(
            title = res.getString(R.string.habit_coasting_title, raw.coastingCount),
            description = res.getString(R.string.habit_coasting_low_desc),
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            color = CardHighlightType.ALERT,
        )
        HabitType.GOOD_COASTING -> SummaryUiModel(
            title = res.getString(R.string.habit_coasting_title, raw.coastingCount),
            description = res.getString(R.string.habit_coasting_desc_good, raw.coastingTimeMillis/1000),
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            color = CardHighlightType.SUCCESS,
        )
        HabitType.HIGH_IDLING -> SummaryUiModel(
            title = res.getString(R.string.habit_idling_bad_title, raw.idlingCount),
            description = res.getString(R.string.habit_idling_bad_desc,raw.idlingTimeMillis),
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            color = CardHighlightType.ALERT,
        )
        HabitType.GOOD_CRUISE -> SummaryUiModel(
            title = res.getString(R.string.habit_cruise_good_title, raw.cruiseCount),
            description = res.getString(R.string.habit_cruise_good_desc, raw.cruiseTimeMillis/1000),
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            color = CardHighlightType.SUCCESS,
        )
        HabitType.NORMAL_INFO -> SummaryUiModel(
            title = res.getString(R.string.habit_normal_info_title),
            description = res.getString(R.string.habit_normal_info_desc),
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            color = CardHighlightType.INFO,
        )
    }
    private fun mapToTip(type: TipType, raw: DrivingHabitSummary): RecommendationUiModel = when(type) {
        TipType.GENTLE_START -> RecommendationUiModel(
            title = res.getString(R.string.rec_tip_gentle_start_title),
            description = res.getString(R.string.rec_tip_gentle_start_desc),
            icon = Icons.Default.Speed
        )
        TipType.STOP_IDLING -> {
            val idlingMins = raw.idlingTimeMillis / 60000L
            RecommendationUiModel(
                title = res.getString(R.string.rec_tip_no_idling_title),
                description = res.getString(R.string.rec_tip_idling_info, idlingMins),
                icon = Icons.Default.Speed
            )
        }
        TipType.STEADY_SPEED -> RecommendationUiModel(
            title = res.getString(R.string.rec_tip_steady_speed_title),
            description = res.getString(R.string.rec_tip_steady_speed_desc),
            icon = Icons.Default.Speed
        )
        TipType.COASTING_MORE -> RecommendationUiModel(
            title = res.getString(R.string.rec_tip_coasting_title),
            description = res.getString(R.string.rec_tip_coasting_desc),
            icon = Icons.Default.EnergySavingsLeaf
        )
    }
}


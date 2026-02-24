package com.example.fuelcompare.presentation.detail

import com.example.domain.model.TripStatistics
import javax.inject.Inject

class DetailReducer @Inject constructor() {
    fun reduce(state: DetailState, event: DetailEvent): DetailState {
        return when (event) {
            is DetailEvent.ChangePeriod -> state.copy(selectedPeriod = event.period, isLoading = true)
            is DetailEvent.StatsLoaded -> {
                state.copy(
                    isLoading = false,
                    chartData = event.filledData, // 이미 가공되었으므로 바로 할당
                    selectedPoint = event.filledData.lastOrNull { it.avgEfficiency != null } ?: event.filledData.last()
                )
            }
            is DetailEvent.SelectPoint -> state.copy(selectedPoint = event.point)
        }
    }

}
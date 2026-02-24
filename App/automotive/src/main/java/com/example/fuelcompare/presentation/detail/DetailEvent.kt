package com.example.fuelcompare.presentation.detail

import com.example.domain.model.TripStatistics

sealed interface DetailEvent {
    data class ChangePeriod(val period: DisplayPeriod) : DetailEvent
    data class StatsLoaded(val filledData: List<ChartPoint>) : DetailEvent
    data class SelectPoint(val point: ChartPoint) : DetailEvent
}
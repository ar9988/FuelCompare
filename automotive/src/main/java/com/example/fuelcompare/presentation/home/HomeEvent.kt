package com.example.fuelcompare.presentation.home

import com.example.domain.model.FuelEfficiencyState
import com.example.domain.model.TripState

sealed class HomeEvent {
    data class UpdateData(val fuelEfficiency: FuelEfficiencyState) : HomeEvent()
    data class UpdateTripState(val trip: TripState) : HomeEvent()
}
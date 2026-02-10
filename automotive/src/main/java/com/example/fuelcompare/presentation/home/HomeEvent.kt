package com.example.fuelcompare.presentation.home

import com.example.domain.model.VehicleGearState

sealed class HomeEvent {
    data class UpdateData(val fuelEfficiency: Float) : HomeEvent()
    data class UpdateGearState(val gear: VehicleGearState) : HomeEvent()
}
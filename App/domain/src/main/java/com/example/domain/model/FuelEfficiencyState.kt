package com.example.domain.model

sealed class FuelEfficiencyState {
    data object Initializing : FuelEfficiencyState()
    data class Ready(val efficiency: Float) : FuelEfficiencyState()
}

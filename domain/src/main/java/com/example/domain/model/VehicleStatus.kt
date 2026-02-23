package com.example.domain.model

data class VehicleStatus(
    val efficiency: FuelEfficiencyState,
    val alert: SpeechTag
)
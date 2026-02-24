package com.example.domain.model

enum class VehicleGearState {
    UNDEFINED,
    PARK,
    REVERSE,
    NEUTRAL,
    DRIVE;

    companion object {
        fun fromInt(value: Int): VehicleGearState {
            return when (value) {
                1 -> NEUTRAL
                2 -> REVERSE
                4 -> PARK
                8 -> DRIVE
                else -> UNDEFINED
            }
        }
    }
}
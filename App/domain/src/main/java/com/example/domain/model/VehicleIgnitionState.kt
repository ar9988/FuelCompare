package com.example.domain.model

enum class VehicleIgnitionState {
    UNDEFINED, LOCK, OFF, ACC, ON, START;

    companion object {
        fun fromInt(value: Int): VehicleIgnitionState {
            return when (value) {
                1 -> LOCK
                2 -> OFF
                3 -> ACC
                4 -> ON
                5 -> START
                else -> UNDEFINED
            }
        }
    }
}
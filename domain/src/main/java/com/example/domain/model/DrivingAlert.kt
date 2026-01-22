package com.example.domain.model

enum class DrivingAlert() {
    NORMAL(),
    HARSH_ACCEL(),
    HARSH_BRAKE(),
    INERTIAL_DRIVING(),
    CONSTANT_SPEED_DRIVING(),
    EXCESSIVE_IDLING()
}
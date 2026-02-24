package com.example.domain.model

enum class SpeechTag(val cooldown: Long) {
    NORMAL(0L),
    HARSH_ACCEL(10000L),
    HARSH_BRAKE(10000L),
    INERTIAL_DRIVING(30000L),
    CONSTANT_SPEED_DRIVING(30000L),
    EXCESSIVE_IDLING(30000L),
    VOICE_COMMAND(0L),
    SYSTEM(0L)
}
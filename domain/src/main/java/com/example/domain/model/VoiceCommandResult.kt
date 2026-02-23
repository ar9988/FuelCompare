package com.example.domain.model

sealed class VoiceCommandResult {
    data class FuelEfficiency(val value: Float) : VoiceCommandResult()
    data object Misunderstood : VoiceCommandResult()
    data object NotStarted : VoiceCommandResult()
    data class Error(val message: String) : VoiceCommandResult()
    data object Calculating : VoiceCommandResult()
}
package com.example.domain.model

sealed class TripEndResult {
    data object Success : TripEndResult()
    data class Error(val message: String) : TripEndResult()
}
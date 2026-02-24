package com.example.domain.model

sealed class TripState {
    data object Idle : TripState()          // PARK 대기
    data object Driving : TripState()       // D / R
    data class Finished(val summary: TripHistory) : TripState()   // D -> P 트리거
}
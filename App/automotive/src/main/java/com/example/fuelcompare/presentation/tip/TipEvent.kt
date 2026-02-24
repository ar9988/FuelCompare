package com.example.fuelcompare.presentation.tip

import com.example.domain.model.HabitAnalysisResult

sealed interface TipEvent {
    data object LoadData : TipEvent
    data class DataLoaded(val result: HabitAnalysisResult) : TipEvent
    data class Error(val message: String) : TipEvent
}
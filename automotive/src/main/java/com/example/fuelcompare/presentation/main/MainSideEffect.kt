package com.example.fuelcompare.presentation.main

sealed class MainSideEffect {
    data object NavigateToSummary: MainSideEffect()
}
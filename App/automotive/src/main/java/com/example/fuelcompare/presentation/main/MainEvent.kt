package com.example.fuelcompare.presentation.main

sealed class MainEvent{
    data object StartListening: MainEvent()
    data object StopListening: MainEvent()
}

sealed interface MainAction {
    data object StartListening : MainAction
    data object StopListening : MainAction
}
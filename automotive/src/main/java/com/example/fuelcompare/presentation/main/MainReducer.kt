package com.example.fuelcompare.presentation.main

import javax.inject.Inject

class MainReducer @Inject constructor(){
    fun reduce(
        mainState: MainState,
        mainAction: MainAction
    ) : MainState = when(mainAction) {
        MainAction.StartListening -> {
            mainState.copy(isListening = true)
        }
        MainAction.StopListening -> {
            mainState.copy(isListening = false)
        }
        else -> {
            mainState
        }
    }
}
package com.example.fuelcompare.presentation.tip

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.AnalyzeDrivingHabitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TipViewModel @Inject constructor(
    private val analyzeDrivingHabitsUseCase: AnalyzeDrivingHabitsUseCase,
    private val reducer: TipReducer
) : ViewModel() {

    private val _uiState = MutableStateFlow(TipState())
    val uiState = _uiState.asStateFlow()

    init {
        handleEvent(TipEvent.LoadData)
    }

    fun handleEvent(event: TipEvent) {
        viewModelScope.launch {
            when (event) {
                is TipEvent.LoadData -> {
                    _uiState.value = reducer.reduce(_uiState.value, event)
                    try {
                        val summary = analyzeDrivingHabitsUseCase()
                        Log.d("summary",summary.toString())
                        handleEvent(TipEvent.DataLoaded(summary))
                    } catch (e: Exception) {
                        handleEvent(TipEvent.Error(e.message ?: "Unknown Error"))
                    }
                }
                else -> {
                    _uiState.value = reducer.reduce(_uiState.value, event)
                }
            }
        }
    }
}
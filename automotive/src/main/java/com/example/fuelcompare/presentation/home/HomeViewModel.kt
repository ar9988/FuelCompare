package com.example.fuelcompare.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetFuelEfficiencyUseCase
import com.example.domain.usecase.GetGearUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFuelEfficiencyUseCase: GetFuelEfficiencyUseCase,
    private val getGearUseCase: GetGearUseCase,
    private val homeReducer: HomeReducer
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        // 시동 상태 구독
        viewModelScope.launch {
            //gear 로 시작 측정하게 변경
            getGearUseCase().collect { gearState ->
                _uiState.value = homeReducer.reduce(_uiState.value, HomeEvent.UpdateGearState(gearState))
            }
        }

        // 연비 데이터 구독 (공회전 연료 소모 포함)
        viewModelScope.launch {
            getFuelEfficiencyUseCase().collect { efficiency ->

                Log.d("DEBUG_VM", "📥 Received Efficiency: $efficiency") // 🔍 로그 3
                _uiState.value = homeReducer.reduce(_uiState.value, HomeEvent.UpdateData(efficiency))
            }
        }
    }
}
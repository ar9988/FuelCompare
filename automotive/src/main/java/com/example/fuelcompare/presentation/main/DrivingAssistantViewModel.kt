package com.example.fuelcompare.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.DrivingAlert
import com.example.domain.model.TripEndResult
import com.example.domain.model.VoiceCommandResult
import com.example.domain.usecase.MonitorCoastingUseCase
import com.example.domain.usecase.MonitorCruiseDrivingUseCase
import com.example.domain.usecase.MonitorDrivingHabitUseCase
import com.example.domain.usecase.MonitorIdlingUseCase
import com.example.domain.usecase.MonitorTripLifecycleUseCase
import com.example.domain.usecase.ProcessFuelVoiceCommandUseCase
import com.example.domain.usecase.SpeakTextUseCase
import com.example.domain.util.ResourceProvider
import com.example.fuelcompare.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrivingAssistantViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val monitorDrivingHabitUseCase: MonitorDrivingHabitUseCase,
    private val processFuelVoiceCommandUseCase: ProcessFuelVoiceCommandUseCase,
    private val monitorCoastingUseCase: MonitorCoastingUseCase,
    private val monitorIdlingUseCase: MonitorIdlingUseCase,
    private val monitorCruiseDrivingUseCase: MonitorCruiseDrivingUseCase,
    private val monitorTripLifecycleUseCase: MonitorTripLifecycleUseCase,
    private val speakTextUseCase: SpeakTextUseCase,
    private val mainReducer: MainReducer
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainState())
    val uiState = _uiState.asStateFlow()

    init {
        startMonitoring()
        observeTripEnd()
    }

    private fun dispatch(action: MainAction) {
        _uiState.value = mainReducer.reduce(_uiState.value, action)
    }

    fun handleIntent(intent: MainEvent) {
        when (intent) {
            MainEvent.StartListening -> {
                dispatch(MainAction.StartListening)
                viewModelScope.launch {
                    val result = processFuelVoiceCommandUseCase().first()
                    handleCommand(result)
                    dispatch(MainAction.StopListening)
                }
            }
            MainEvent.StopListening ->{
                dispatch(MainAction.StopListening)
            }
            else -> {

            }
        }
    }

    private fun observeTripEnd() {
        viewModelScope.launch {
            monitorTripLifecycleUseCase().collect { result ->
                when (result) {
                    is TripEndResult.Success -> {
                        Log.d("ViewModel", "운행 종료 및 데이터 저장 완료")
                        // 필요하다면 UI에 "운행 요약이 저장되었습니다" 팝업 알림 Action 전송
                    }
                    is TripEndResult.Error -> {
                        Log.e("ViewModel", "저장 실패: ${result.message}")
                    }
                }
            }
        }
    }

    private fun startMonitoring() {
        // 1. 급가속/급감속 모니터링
        viewModelScope.launch {
            monitorDrivingHabitUseCase().collect { alert ->
                handleAlert(alert)
            }
        }

        // 2. 공회전 모니터링
        viewModelScope.launch {
            monitorIdlingUseCase().collect { alert ->
                handleAlert(alert)
            }
        }

        // 3. 정속 주행 모니터링
        viewModelScope.launch {
            monitorCruiseDrivingUseCase().collect { alert ->
                handleAlert(alert)
            }
        }

        // 4. 타력 주행 모니터링
        viewModelScope.launch {
            monitorCoastingUseCase().collect { alert ->
                handleAlert(alert)
            }
        }

        // 5. 주행 종료 모니터링
        viewModelScope.launch {

        }
    }

    private fun handleCommand(result: VoiceCommandResult){
        when (result){
            is VoiceCommandResult.FuelEfficiency ->{
                speakTextUseCase(resourceProvider.getString(R.string.fuel_efficiency_text,result.value))
            }
            is VoiceCommandResult.Error -> {
                speakTextUseCase(result.message)
            }
            is VoiceCommandResult.Misunderstood -> {

            }
        }
    }

    private fun handleAlert(alert: DrivingAlert) {
        if (alert != DrivingAlert.NORMAL) {
            val message = getAlertMessage(alert)
            speakTextUseCase(message)
        }
    }

    private fun getAlertMessage(alert: DrivingAlert) : String {
        val resId = when (alert) {
            DrivingAlert.NORMAL -> (R.string.driving_event_normal)
            DrivingAlert.HARSH_ACCEL -> (R.string.driving_event_harsh_accel)
            DrivingAlert.HARSH_BRAKE -> (R.string.driving_event_harsh_brake)
            DrivingAlert.INERTIAL_DRIVING -> (R.string.driving_event_inertial_driving)
            DrivingAlert.CONSTANT_SPEED_DRIVING -> (R.string.driving_event_constant_speed_driving)
            DrivingAlert.EXCESSIVE_IDLING -> (R.string.driving_event_excessive_idling)
            else -> R.string.driving_event_normal
        }
        return resourceProvider.getString(resId)
    }
}
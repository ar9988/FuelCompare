package com.example.fuelcompare.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.SpeechTag
import com.example.domain.model.TripState
import com.example.domain.model.VoiceCommandResult
import com.example.domain.usecase.MonitorTripLifecycleUseCase
import com.example.domain.usecase.ProcessFuelVoiceCommandUseCase
import com.example.domain.usecase.SpeakTextUseCase
import com.example.domain.usecase.VehicleMonitoringUseCase
import com.example.domain.util.ResourceProvider
import com.example.fuelcompare.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrivingAssistantViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val vehicleMonitoringUseCase: VehicleMonitoringUseCase,
    private val processFuelVoiceCommandUseCase: ProcessFuelVoiceCommandUseCase,
    private val monitorTripLifecycleUseCase: MonitorTripLifecycleUseCase,
    private val speakTextUseCase: SpeakTextUseCase,
    private val mainReducer: MainReducer,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainState())
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = Channel<MainSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        startMonitoring()
    }

    private fun dispatch(action: MainAction) {
        _uiState.value = mainReducer.reduce(_uiState.value, action)
    }

    fun handleIntent(intent: MainEvent) {
        when (intent) {
            MainEvent.StartListening -> {
                dispatch(MainAction.StartListening)
                viewModelScope.launch {
                    val result = processFuelVoiceCommandUseCase(resourceProvider.getString(R.string.trigger_voice)).first()
                    handleCommand(result)
                    dispatch(MainAction.StopListening)
                }
            }

            MainEvent.StopListening -> {
                dispatch(MainAction.StopListening)
            }
        }
    }

    private fun startMonitoring() {
        // 차량 모니터링 시작
        viewModelScope.launch {
            vehicleMonitoringUseCase().collect { vehicleState ->
                handleAlert(vehicleState.alert)
            }
        }

        //주행 생명주기 모니터링
        viewModelScope.launch {
            monitorTripLifecycleUseCase().collect { state ->
                if (state is TripState.Finished) {
                    _sideEffect.send(MainSideEffect.NavigateToSummary)
                    speakTextUseCase(resourceProvider.getString(R.string.trip_end_text),SpeechTag.SYSTEM)
                }
            }
        }
    }

    private fun handleCommand(result: VoiceCommandResult) {
        val tag = SpeechTag.VOICE_COMMAND
        when (result) {
            is VoiceCommandResult.FuelEfficiency -> {
                speakTextUseCase(
                    resourceProvider.getString(
                        R.string.fuel_efficiency_text,
                        result.value
                    ),
                    tag
                )
            }

            is VoiceCommandResult.Error -> {
                speakTextUseCase(result.message, tag)
            }

            is VoiceCommandResult.Misunderstood -> {
                speakTextUseCase(
                    resourceProvider.getString(R.string.fuel_efficiency_misunderstanding),
                    tag
                )
            }

            is VoiceCommandResult.Calculating -> {
                speakTextUseCase(
                    resourceProvider.getString(R.string.fuel_efficiency_calculating),
                    tag
                )
            }

            is VoiceCommandResult.NotStarted -> {
                speakTextUseCase(
                    resourceProvider.getString(R.string.fuel_efficiency_not_started),
                    tag
                )
            }
        }
    }

    private fun handleAlert(tag: SpeechTag) {
        if (tag != SpeechTag.NORMAL) {
            val message = getAlertMessage(tag)
            speakTextUseCase(message, tag)
        }
    }

    private fun getAlertMessage(alert: SpeechTag): String {
        val resId = when (alert) {
            SpeechTag.NORMAL -> (R.string.driving_event_normal)
            SpeechTag.HARSH_ACCEL -> (R.string.driving_event_harsh_accel)
            SpeechTag.HARSH_BRAKE -> (R.string.driving_event_harsh_brake)
            SpeechTag.INERTIAL_DRIVING -> (R.string.driving_event_inertial_driving)
            SpeechTag.CONSTANT_SPEED_DRIVING -> (R.string.driving_event_constant_speed_driving)
            SpeechTag.EXCESSIVE_IDLING -> (R.string.driving_event_excessive_idling)
            else -> R.string.driving_event_normal
        }
        return resourceProvider.getString(resId)
    }
}
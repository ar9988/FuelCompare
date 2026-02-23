package com.example.domain.usecase

import com.example.domain.model.FuelEfficiencyState
import com.example.domain.model.SpeechState
import com.example.domain.model.TripState
import com.example.domain.model.VoiceCommandResult
import com.example.domain.service.SpeechService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProcessFuelVoiceCommandUseCase @Inject constructor(
    private val getFuelEfficiencyUseCase: GetFuelEfficiencyUseCase,
    private val getTripStateUseCase: GetTripStateUseCase,
    private val speechService: SpeechService,
) {
    operator fun invoke(target: String): Flow<VoiceCommandResult> = callbackFlow {
        val collectJob = launch {
            speechService.startListening().collect { state ->
                when (state) {
                    is SpeechState.Success -> {
                        val text = state.text

                        if (text.contains(target)) {
                            val tripState = getTripStateUseCase().first()
                            when {
                                tripState is TripState.Driving -> {
                                    when (val fuelEfficiency = getFuelEfficiencyUseCase().first()) {
                                        is FuelEfficiencyState.Ready -> {
                                            trySend(VoiceCommandResult.FuelEfficiency(fuelEfficiency.efficiency))
                                        }

                                        is FuelEfficiencyState.Initializing -> {
                                            trySend(VoiceCommandResult.Calculating)
                                        }
                                    }
                                }
                                else -> {
                                    trySend(VoiceCommandResult.NotStarted)
                                }
                            }
                            close()
                        } else {
                            trySend(VoiceCommandResult.Misunderstood)
                            close()
                        }
                    }
                    is SpeechState.Error -> {
                        trySend(VoiceCommandResult.Error(state.message))
                        close()
                    }
                    SpeechState.Listening -> {

                    }
                    else -> {}
                }
            }
        }

        awaitClose {
            collectJob.cancel()
            speechService.stopListening()
        }
    }
}
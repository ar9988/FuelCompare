package com.example.domain.usecase

import com.example.domain.model.SpeechState
import com.example.domain.model.VoiceCommandResult
import com.example.domain.repository.CarRepository
import com.example.domain.service.SpeechService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProcessFuelVoiceCommandUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val speechService: SpeechService
) {
    operator fun invoke(): Flow<VoiceCommandResult> = callbackFlow {
        // 1. 음성 인식 시작
        val collectJob = launch{
            speechService.startListening().collect { state ->
                when (state) {
                    is SpeechState.Success -> {
                        val text = state.text
                        if (text.contains("연비")) {
                            val efficiency = carRepository.getEfficiency().first()
                            trySend(VoiceCommandResult.FuelEfficiency(efficiency))
                        } else {
                            trySend(VoiceCommandResult.Misunderstood)
                        }
                        close()
                    }
                    is SpeechState.Error -> {
                        trySend(VoiceCommandResult.Error(state.message))
                        close()
                    }
                    else -> { // 음성 입력 대기상태
                    }
                }
            }
        }
        awaitClose {
            collectJob.cancel() // 수집 중단
            speechService.stopListening()
        }
    }
}
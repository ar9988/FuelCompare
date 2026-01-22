package com.example.domain.service

import com.example.domain.model.SpeechState
import kotlinx.coroutines.flow.Flow

interface SpeechService {
    // STT: 음성 인식을 시작하고 결과를 Flow로 반환
    fun startListening(): Flow<SpeechState>
    fun stopListening()

    // TTS: 텍스트를 음성으로 출력
    fun speak(text: String)
    fun shutdown()
}
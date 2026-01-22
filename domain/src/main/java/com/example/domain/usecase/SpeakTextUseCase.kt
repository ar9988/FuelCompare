package com.example.domain.usecase

import com.example.domain.service.SpeechService
import javax.inject.Inject

class SpeakTextUseCase @Inject constructor(
    private val speechService: SpeechService
) {
    operator fun invoke(text: String) {
        speechService.speak(text)
    }
}
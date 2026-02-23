package com.example.domain.usecase

import com.example.domain.model.SpeechTag
import com.example.domain.service.SpeechService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeakTextUseCase @Inject constructor(
    private val speechService: SpeechService
) {
    private val lastSpokenMap = java.util.EnumMap<SpeechTag, Long>(SpeechTag::class.java)

    operator fun invoke(text: String, tag: SpeechTag) {
        val currentTime = System.currentTimeMillis()
        val lastTime = lastSpokenMap[tag] ?: 0L

        // Enum에 설정된 쿨타임 정책을 바로 적용
        if (currentTime - lastTime >= tag.cooldown) {
            speechService.speak(text)
            lastSpokenMap[tag] = currentTime
        }
    }
}
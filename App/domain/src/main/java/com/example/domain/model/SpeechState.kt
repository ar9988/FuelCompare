package com.example.domain.model

sealed class SpeechState {
    data object Idle : SpeechState()
    data object Listening : SpeechState()
    data class Success(val text: String) : SpeechState()
    data class Error(val message: String) : SpeechState()
}
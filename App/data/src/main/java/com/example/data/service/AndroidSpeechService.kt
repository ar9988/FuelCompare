package com.example.data.service

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.domain.model.SpeechState
import com.example.domain.service.SpeechService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.Locale
import javax.inject.Inject

class AndroidSpeechService @Inject constructor(
    @ApplicationContext private val context: Context
) : SpeechService, RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private val _speechState = MutableSharedFlow<SpeechState>(
        replay = 0,
        extraBufferCapacity = 1
    )

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // TTS 음성 속성 설정 (음악 소리 줄이기/덕킹)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                tts?.setAudioAttributes(audioAttributes)
                tts?.language = Locale.KOREAN
            }
        }
    }

    override fun startListening(): Flow<SpeechState> {
        Handler(Looper.getMainLooper()).post {
            // 기존 리스너가 있다면 취소
            speechRecognizer?.cancel()

            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@AndroidSpeechService)
                }
            }

            Log.d("startListening","listening Start")
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
            }
            speechRecognizer?.startListening(intent)
        }
        return _speechState
    }

    override fun stopListening() {
        Handler(Looper.getMainLooper()).post {
            speechRecognizer?.stopListening()
            _speechState.tryEmit(SpeechState.Idle)
        }
    }

    override fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "TTS_ID_${System.currentTimeMillis()}")
    }

    override fun shutdown() {
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _speechState.tryEmit(SpeechState.Success(matches[0]))
        }
    }

    override fun onRmsChanged(rmsdB: Float) {
    }

    override fun onBeginningOfSpeech() {
    }

    override fun onBufferReceived(buffer: ByteArray?) {
    }

    override fun onEndOfSpeech() {
        _speechState.tryEmit(SpeechState.Idle)
    }

    override fun onError(error: Int) {
        Log.d("SpeechService",error.toString())
        val message = when (error) {
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한 없음"
            SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
            else -> "에러 발생: $error"
        }
        _speechState.tryEmit(SpeechState.Error(message))
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
    }

    override fun onPartialResults(partialResults: Bundle?) {
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _speechState.tryEmit(SpeechState.Listening)
    }

}
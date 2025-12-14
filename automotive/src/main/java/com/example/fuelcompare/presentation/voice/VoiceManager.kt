package com.example.fuelcompare.presentation.voice

import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.listener.ResultListener
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceManager @Inject constructor(
    @ApplicationContext private val context: Context
){
    private val stt = SpeechToText(context, ai.pleos.playground.stt.constant.Mode.ON_DEVICE)

    fun init() {
        stt.initialize()
        stt.addListener(listener)
        Log.d("Voice Manager","init")
    }

    fun startAlwaysOn() {
        stt.request() // 첫 시작
    }

    fun release() {
        stt.stop()   // 음성 인식 중지
        stt.removeListener(listener)
        Log.d("Voice Manager","release")
    }

    private val listener = object : ResultListener {

        override fun onUpdated(stt: String, completed: Boolean) {
            if (completed) {
                handleRecognizedSentence(stt)

                // 다음 명령을 위해 다시 듣기 시작
                this@VoiceManager.stt.request()
            }
        }

        override fun onUpdatedEpdData(on: Long, off: Long) {
            TODO("Not yet implemented")
        }

        override fun onError() {
            // 에러가 나도 끊기지 않고 계속 듣기 유지
            stt.request()
        }

        override fun onReady() {
            TODO("Not yet implemented")
        }

        override fun onStartedRecognition() {
            TODO("Not yet implemented")
        }

        override fun onEndedRecognition() {
            stt.request()
        }
    }

    private fun handleRecognizedSentence(sentence: String) {
        Log.d("VoiceManager", "Recognized sentence: $sentence")

        if (sentence.contains("연비")) {
            Log.d("VoiceManager", "Fuel command detected!")
            onFuelCommand?.invoke()
        }
    }

    var onFuelCommand: (() -> Unit)? = null
}
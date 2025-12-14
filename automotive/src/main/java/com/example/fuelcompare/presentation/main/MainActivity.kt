package com.example.fuelcompare.presentation.main

import androidx.activity.compose.setContent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.fuelcompare.presentation.theme.MyAppTheme
import com.example.fuelcompare.presentation.voice.VoiceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var voiceManager: VoiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        voiceManager.init()
        voiceManager.startAlwaysOn()
        setContent {
            MyAppTheme{
                AppRoot()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.release()
    }
}
package com.example.fuelcompare.feature

import androidx.activity.compose.setContent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.fuelcompare.feature.main.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}
package com.example.fuelcompare.presentation.main

import androidx.activity.compose.setContent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.fuelcompare.presentation.theme.MyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAppTheme{
                MyApp()
            }
        }
    }
}
package com.example.fuelcompare.presentation.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun DetailScreen(navController: NavController) {
    Column {
        Text("Detail Screen")
        Button(onClick = { navController.navigate("home") }) {
            Text("Go to Home")
        }
    }
}
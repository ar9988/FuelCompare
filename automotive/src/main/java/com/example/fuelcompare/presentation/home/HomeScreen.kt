package com.example.fuelcompare.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    Column {
        Text("Home Screen")
        Button(onClick = { navController.navigate("detail") }) {
            Text("Go to Detail")
        }
    }
}
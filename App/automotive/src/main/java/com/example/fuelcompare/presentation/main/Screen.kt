package com.example.fuelcompare.presentation.main

sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Home")
    object Detail : Screen("detail", "Detail")
    object Tip : Screen("tip","Tip")
}
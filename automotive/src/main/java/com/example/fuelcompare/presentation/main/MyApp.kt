package com.example.fuelcompare.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fuelcompare.presentation.detail.DetailScreen
import com.example.fuelcompare.presentation.home.HomeScreen
import com.example.fuelcompare.presentation.tip.TipScreen

@Composable
fun MyApp() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Detail,
        Screen.Tip
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentDestination = navController
                    .currentBackStackEntryFlow
                    .collectAsState(initial = navController.currentBackStackEntry)
                    .value?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        selected = currentDestination?.route == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Detail.route) { DetailScreen(navController) }
            composable(Screen.Tip.route) { TipScreen(navController) }
        }
    }
}

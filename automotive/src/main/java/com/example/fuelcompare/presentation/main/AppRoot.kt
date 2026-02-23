package com.example.fuelcompare.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fuelcompare.presentation.detail.DetailScreen
import com.example.fuelcompare.presentation.home.HomeScreen
import com.example.fuelcompare.presentation.theme.appColors
import com.example.fuelcompare.presentation.tip.TipScreen

@Composable
fun AppRoot(
    navController: NavHostController,
    onListeningButtonClick: () -> Unit,
    isListening: Boolean
) {
    val items = listOf(
        Screen.Home,
        Screen.Detail,
        Screen.Tip
    )

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.appColors.bgGradientStart,
            MaterialTheme.appColors.bgGradientEnd
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    // 인식 중일 때 나타나는 녹색 원형 진행 바
                    if (isListening) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(72.dp),
                            color = Color.Green,
                            strokeWidth = 4.dp
                        )
                    }

                    // 음성 인식 시작 버튼
                    FloatingActionButton(
                        onClick = onListeningButtonClick,
                        containerColor = if (isListening) Color.DarkGray else MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                            contentDescription = "음성 인식",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                        shadowElevation = 8.dp,
                        modifier = Modifier.height(72.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items.forEach { screen ->
                                val isSelected = currentRoute == screen.route
                                val icon = when (screen) {
                                    is Screen.Home -> Icons.Default.DirectionsCar
                                    is Screen.Detail -> Icons.Default.BarChart
                                    is Screen.Tip -> Icons.Default.Lightbulb
                                    else -> Icons.Default.DirectionsCar
                                }

                                ModernBottomNavItem(
                                    label = screen.label,
                                    icon = icon,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (!isSelected) {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(Screen.Home.route) { HomeScreen(navController) }
                composable(Screen.Detail.route) { DetailScreen(navController) }
                composable(Screen.Tip.route) { TipScreen(navController) }
            }
        }
    }

}

@Composable
fun RowScope.ModernBottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .weight(if (isSelected) 1.5f else 1f)
            .padding(6.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )

            AnimatedVisibility(
                visible = isSelected,
                enter = expandHorizontally(animationSpec = tween(300)),
                exit = shrinkHorizontally(animationSpec = tween(300))
            ) {
                Text(
                    text = label,
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
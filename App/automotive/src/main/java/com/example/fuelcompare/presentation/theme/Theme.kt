package com.example.fuelcompare.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


private val LightColors = lightColorScheme(
    primary = solid_light_gray500,
    background = solid_light_gray00,
    surface = solid_light_gray50,
    onPrimary = solid_light_gray00,
    onBackground = solid_light_gray900,
    onSurface = solid_light_gray900
)

private val DarkColors = darkColorScheme(
    primary = solid_dark_gray500,
    background = solid_dark_gray00,
    surface = solid_dark_gray50,
    onPrimary = solid_dark_gray00,
    onBackground = solid_dark_gray900,
    onSurface = solid_dark_gray900
)

@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    androidx.compose.runtime.CompositionLocalProvider(
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = AppTypography,
            content = content
        )
    }
}
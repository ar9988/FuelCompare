package com.example.fuelcompare.presentation.theme


import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val alphaGray50: Color,
    val alphaGray100: Color,
    val alphaWhite50: Color,
    val alphaWhite100: Color,


    // Informative 색상
    val informativeActive: Color,
    val informativePositive: Color,
    val informativeNegative: Color,
    val informativeAutonomousDriving: Color,

    // Regulation 색상
    val regulationBlue: Color,
    val regulationGreen: Color,
    val regulationYellow: Color,
    val regulationOrange: Color,
    val regulationRed: Color
)

val LightAppColors = AppColors(
    alphaGray50 = alpha_light_gray50,
    alphaGray100 = alpha_light_gray100,
    alphaWhite50 = alpha_light_white50,
    alphaWhite100 = alpha_light_white100,

    // Informative 매핑
    informativeActive = informative_active_light,
    informativePositive = informative_positive_light,
    informativeNegative = informative_negative_light,
    informativeAutonomousDriving = informative_autonomous_driving_light,

    // Regulation 매핑
    regulationBlue = regulation_blue_light,
    regulationGreen = regulation_green_light,
    regulationYellow = regulation_yellow_light,
    regulationOrange = regulation_orange_light,
    regulationRed = regulation_red_light
)

val DarkAppColors = AppColors(
    alphaGray50 = alpha_dark_gray50,
    alphaGray100 = alpha_dark_gray100,
    alphaWhite50 = alpha_dark_white50,
    alphaWhite100 = alpha_dark_white100,

    // Informative 매핑
    informativeActive = informative_active_dark,
    informativePositive = informative_positive_dark,
    informativeNegative = informative_negative_dark,
    informativeAutonomousDriving = informative_autonomous_driving_dark,

    // Regulation 매핑
    regulationBlue = regulation_blue_dark,
    regulationGreen = regulation_green_dark,
    regulationYellow = regulation_yellow_dark,
    regulationOrange = regulation_orange_dark,
    regulationRed = regulation_red_dark
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
val MaterialTheme.appColors: AppColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current
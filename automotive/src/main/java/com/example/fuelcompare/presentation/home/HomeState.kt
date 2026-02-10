package com.example.fuelcompare.presentation.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.fuelcompare.R
import com.example.fuelcompare.presentation.theme.appColors

sealed interface HomeState {
    data object Loading : HomeState               // 초기 앱 로딩
    data object WaitingForIgnition : HomeState    // 시동 꺼짐 (분석 대기)
    data object Initializing : HomeState          // 시동은 켰으나 데이터 수집 시작 전 (0.0km/L)
    data class Success(
        val fuelEfficiency: Float,
        val grade: FuelGrade
    ) : HomeState
    data class Error(val message: String) : HomeState
}

enum class FuelGrade(
    val titleRes: Int,
    val descriptionRes: Int,
    val color: @Composable () -> Color,
    val icon: ImageVector
) {
    EXCELLENT(R.string.grade_excellent, R.string.desc_excellent, { androidx.compose.material3.MaterialTheme.appColors.informativeActive }, Icons.Default.Eco),
    NORMAL(R.string.grade_normal, R.string.desc_normal, { androidx.compose.material3.MaterialTheme.appColors.informativePositive }, Icons.Default.DirectionsCar),
    POOR(R.string.grade_poor, R.string.desc_poor, { androidx.compose.material3.MaterialTheme.appColors.informativeNegative }, Icons.Default.Warning)
}

// 연비 값에 따라 등급을 결정하는 함수
fun getFuelGrade(efficiency: Float): FuelGrade {
    return when {
        efficiency >= 15f -> FuelGrade.EXCELLENT
        efficiency >= 10f -> FuelGrade.NORMAL
        else -> FuelGrade.POOR
    }
}
package com.example.fuelcompare.presentation.tip

import androidx.compose.ui.graphics.vector.ImageVector

data class SummaryUiModel(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: CardHighlightType,
)

data class RecommendationUiModel(
    val title: String,
    val description: String,
    val icon: ImageVector
)

enum class CardHighlightType {
    ALERT,   // 빨간색 계열 (regulationRed)
    SUCCESS, // 초록색 계열 (regulationGreen)
    INFO     // 회색/기본 계열
}
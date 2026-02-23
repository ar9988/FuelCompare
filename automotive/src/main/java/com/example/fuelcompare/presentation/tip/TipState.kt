package com.example.fuelcompare.presentation.tip

data class TipState(
    val isLoading: Boolean = true,
    val summaries: List<SummaryUiModel> = emptyList(),
    val recommendations: List<RecommendationUiModel> = emptyList(),
)
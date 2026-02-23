package com.example.fuelcompare.presentation.tip

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fuelcompare.R
import com.example.fuelcompare.presentation.theme.appColors

@Composable
fun TipScreen(
    navController: NavController,
    viewModel: TipViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.tip_screen_summary_title),
                        style = MaterialTheme.typography.titleLarge
                    )

                    // 상단 요약 카드 영역
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max), // 💡 높이를 자식 중 최대값으로 고정
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        uiState.summaries.forEach { summary ->
                            HabitSummaryCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(), // 💡 최대 높이까지 채움
                                title = summary.title,
                                description = summary.description,
                                icon = summary.icon,
                                cardColor = when (summary.color) {
                                    CardHighlightType.ALERT -> MaterialTheme.appColors.regulationRed
                                    CardHighlightType.SUCCESS -> MaterialTheme.appColors.regulationGreen
                                    else -> MaterialTheme.appColors.alphaGray100
                                },
                                borderColor = MaterialTheme.appColors.alphaWhite100,
                                iconDesc = ""
                            )
                        }
                    }

                    Text(
                        text = stringResource(id = R.string.tip_screen_recommendation_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    // 💡 하단 추천 팁 카드 영역 수정
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max), // 💡 Row의 높이를 가장 긴 카드의 높이에 맞춤
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.recommendations.forEach { tip ->
                            RecommendationTipCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(), // 💡 결정된 Row의 높이만큼 꽉 채움
                                title = tip.title,
                                description = tip.description,
                                icon = tip.icon
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun HabitSummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector,
    iconDesc: String,
    cardColor: Color,
    borderColor: Color
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .background(cardColor)
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconDesc,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(40.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun RecommendationTipCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.appColors.informativeActive,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

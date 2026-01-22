package com.example.fuelcompare.presentation.tip

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fuelcompare.R
import com.example.fuelcompare.presentation.theme.appColors

@Composable
fun TipScreen(navController: NavController) {
    // 이 화면의 UI를 호출합니다.
    DrivingTipScreen(userName = "OO") // 실제 앱에서는 사용자 정보를 받아와야 합니다.
}

@Composable
fun DrivingTipScreen(userName: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        ) {
            // OO님의 운전 습관 요약
            Text(
                text = stringResource(id = R.string.tip_screen_user_summary_title, userName),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // 운전 습관 요약 카드
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 급가속 카드
                HabitSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.habit_harsh_acceleration_title, 5),
                    description = stringResource(R.string.habit_harsh_acceleration_desc),
                    icon = Icons.Default.   TrendingDown,
                    iconDesc = stringResource(R.string.content_desc_harsh_acceleration),
                    cardColor = MaterialTheme.appColors.regulationRed.copy(alpha = 0.8f),
                    borderColor = MaterialTheme.appColors.regulationRed
                )
                // 타력 주행 카드
                HabitSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.habit_coasting_title, 7),
                    description = stringResource(R.string.habit_coasting_desc),
                    icon = Icons.Default.EnergySavingsLeaf,
                    iconDesc = stringResource(R.string.content_desc_coasting),
                    cardColor = MaterialTheme.appColors.regulationGreen.copy(alpha = 0.8f),
                    borderColor = MaterialTheme.appColors.regulationGreen
                )
            }

            // 연비 개선을 위한 추천 팁
            Text(
                text = stringResource(id = R.string.tip_screen_recommendation_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // 추천 팁 카드 목록
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RecommendationTipCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.rec_tip_gentle_start_title),
                    description = stringResource(R.string.rec_tip_gentle_start_desc),
                    icon = Icons.Default.Speed
                )
                RecommendationTipCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.rec_tip_steady_speed_title),
                    description = stringResource(R.string.rec_tip_steady_speed_desc),
                    icon = Icons.Default.Speed // 아이콘은 디자인에 맞게 변경
                )
                RecommendationTipCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.rec_tip_no_idling_title),
                    description = stringResource(R.string.rec_tip_no_idling_desc),
                    icon = Icons.Default.Speed // 아이콘은 디자인에 맞게 변경
                )
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

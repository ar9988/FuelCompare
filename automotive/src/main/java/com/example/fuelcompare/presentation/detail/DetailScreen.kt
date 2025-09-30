package com.example.fuelcompare.presentation.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fuelcompare.R
import com.example.fuelcompare.presentation.theme.appColors


@Composable
fun DetailScreen(
    navController: NavController
) {
    // 선택된 탭을 관리하는 상태 변수 ('daily' 또는 'monthly')
    var selectedPeriod by remember { mutableStateOf("daily") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // 네비게이션 바가 있다면 그 영역을 피하기 위해 bottom padding 추가 가능
                .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            // 화면 제목
            Text(
                text = stringResource(id = R.string.record_screen_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 일별/월별 토글 버튼
            PeriodToggle(
                selectedPeriod = selectedPeriod,
                onPeriodSelect = { newPeriod -> selectedPeriod = newPeriod }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 연비 차트
            FuelEfficiencyChart()

            Spacer(modifier = Modifier.height(24.dp))

            // 주행 기록 카드 목록
            DrivingRecordList()
        }
    }
}

@Composable
fun PeriodToggle(selectedPeriod: String, onPeriodSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToggleButton(
            text = stringResource(id = R.string.record_toggle_daily),
            isSelected = selectedPeriod == "daily",
            onClick = { onPeriodSelect("daily") }
        )
        ToggleButton(
            text = stringResource(id = R.string.record_toggle_monthly),
            isSelected = selectedPeriod == "monthly",
            onClick = { onPeriodSelect("monthly") }
        )
    }
}

@Composable
fun ToggleButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.appColors.informativeActive.copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.appColors.informativeActive else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val borderColor = if (isSelected) MaterialTheme.appColors.informativeActive else MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50)) // 둥근 모서리 적용
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .background(color = backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun FuelEfficiencyChart() {
    // 실제 앱에서는 ViewModel 등에서 차트 데이터를 받아와야 합니다.
    // 여기서는 UI 재현을 위해 더미 데이터를 사용합니다.
    val dataPoints = listOf(38f, 42f, 45f, 52f, 48f, 65f, 70f, 75f, 85f)
    val yLabels = listOf("0", "50", "100")
    val xLabels = listOf("0", "10:13", "10:23", "20:23", "20:30", "44:28", "20:28", "20:31")
    val chartColor = MaterialTheme.appColors.informativeActive
    val gridColor = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Y축 레이블
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            yLabels.reversed().forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))

        // 차트 본문
        Column(modifier = Modifier.fillMaxSize()) {
            // 차트 그리기 영역
            Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val xStep = size.width / (dataPoints.size - 1)
                val yRange = 100f // Y축 최대값

                // 그리드 라인 그리기
                yLabels.forEach {
                    val y = size.height * (1 - it.toFloat() / yRange)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }

                // 경로 생성 (부드러운 곡선)
                val path = Path()
                path.moveTo(0f, size.height * (1 - dataPoints.first() / yRange))
                for (i in 0 until dataPoints.size - 1) {
                    val x1 = i * xStep
                    val y1 = size.height * (1 - dataPoints[i] / yRange)
                    val x2 = (i + 1) * xStep
                    val y2 = size.height * (1 - dataPoints[i+1] / yRange)
                    path.cubicTo(
                        x1 + xStep / 2, y1,
                        x1 + xStep / 2, y2,
                        x2, y2
                    )
                }

                // 라인 그리기
                drawPath(path, color = chartColor, style = Stroke(width = 5f))

                // 데이터 포인트 그리기
                dataPoints.forEachIndexed { index, value ->
                    drawCircle(
                        color = chartColor,
                        radius = 8f,
                        center = Offset(index * xStep, size.height * (1 - value / yRange))
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            // X축 레이블
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                xLabels.forEach {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DrivingRecordList() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 첫 번째 일반 기록 카드
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.record_card_purpose), style = MaterialTheme.typography.bodyMedium)
                TextWithIcon(text = stringResource(R.string.record_card_duration))
                TextWithIcon(text = stringResource(R.string.record_card_distance_short))
                TextWithIcon(text = stringResource(R.string.record_card_distance_long))
            }
        }

        // 두 번째 성과 기록 카드
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.record_achievement_card_title), style = MaterialTheme.typography.bodyMedium)
                TextWithIcon(text = stringResource(R.string.record_achievement_card_date))
                TextWithIcon(text = stringResource(R.string.record_achievement_card_fuel))
            }
        }
    }
}

@Composable
fun TextWithIcon(text: String, tint: Color = MaterialTheme.appColors.informativeActive) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
    }
}
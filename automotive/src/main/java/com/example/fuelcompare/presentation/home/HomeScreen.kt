package com.example.fuelcompare.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fuelcompare.R
import com.example.fuelcompare.presentation.theme.appColors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun HomeScreen(navController: NavController) {
    FuelEfficiencyDashboard()
}

@Composable
fun FuelEfficiencyDashboard() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        // [적용] 테마의 background 색상 사용
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 상단 로고
            Text(
                text = stringResource(id = R.string.pleos_connect),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 중앙 게이지 및 정보 섹션
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f) // 화면 비율에 따라 크기 조절
            ) {
                FuelGauge(
                    progress = 0.75f, // 0.0 ~ 1.0 사이의 값으로 연비 상태 표현
                    primaryColor = MaterialTheme.appColors.informativeActive
                )
                FuelInfo(
                    fuelEfficiency = "17.2",
                    primaryColor = MaterialTheme.appColors.informativeActive
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 하단 팁 텍스트
            Text(
                text = stringResource(id = R.string.dashboard_tip_message),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// --- 커스텀 연비 게이지 ---
@Composable
fun FuelGauge(
    modifier: Modifier = Modifier,
    progress: Float,
    primaryColor: Color
) {
    // [적용] 테마의 surface 변형 색상(surface보다 약간 어두운) 사용
    val gaugeBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val startAngle = 150f
    val sweepAngle = 240f
    val totalTicks = 50 // 전체 눈금 수

    Canvas(modifier = modifier.fillMaxSize(0.8f)) {

        val diameter = min(size.width, size.height) // 정사각형 크기
        val arcSize = Size(diameter, diameter)
        val topLeft = Offset(
            (size.width - diameter) / 2,
            (size.height - diameter) / 2
        )


        drawArc(
            color = gaugeBackgroundColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = 20f, cap = StrokeCap.Round)
        )

        drawArc(
            color = primaryColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = 20f, cap = StrokeCap.Round)
        )

        // 눈금 그리기 (이하 동일)
        val arcRadius = diameter / 2
        val tickRadius = arcRadius
        for (i in 0..totalTicks) {
            val angleFraction = i.toFloat() / totalTicks
            val angleInDegrees = startAngle + angleFraction * sweepAngle
            val angleInRadians = Math.toRadians(angleInDegrees.toDouble()).toFloat()

            val isMajorTick = i % 5 == 0
            val tickLength = if (isMajorTick) 30f else 15f
            val tickColor = if (angleFraction <= progress) primaryColor else gaugeBackgroundColor

            val start = Offset(
                x = center.x + (tickRadius - tickLength) * cos(angleInRadians),
                y = center.y + (tickRadius - tickLength) * sin(angleInRadians)
            )
            val end = Offset(
                x = center.x + tickRadius * cos(angleInRadians),
                y = center.y + tickRadius * sin(angleInRadians)
            )

            drawLine(
                color = tickColor,
                start = start,
                end = end,
                strokeWidth = if (isMajorTick) 6f else 3f,
                cap = StrokeCap.Round
            )
        }
    }
}


// --- 게이지 중앙 정보 ---
@Composable
fun FuelInfo(fuelEfficiency: String, primaryColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocalGasStation,
            contentDescription = stringResource(id = R.string.content_desc_fuel),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = fuelEfficiency,
                style = MaterialTheme.typography.displayLarge,
                color = primaryColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.unit_km_per_liter),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.offset(x = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = stringResource(id = R.string.content_desc_evaluation),
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                buildAnnotatedString {
                    withStyle(
                        style = MaterialTheme.typography.labelMedium.toSpanStyle().copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        append(stringResource(id = R.string.dashboard_evaluation_title) + "\n")
                    }
                    withStyle(
                        style = MaterialTheme.typography.titleMedium.toSpanStyle().copy(
                            color = primaryColor
                        )
                    ) {
                        append(stringResource(id = R.string.dashboard_evaluation_good))
                    }
                }
            )
        }
    }
}

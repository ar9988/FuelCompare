package com.example.fuelcompare.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fuelcompare.presentation.theme.appColors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


@Composable
fun HomeScreen(navController: NavController) {
    FuelEfficiencyDashboard()
}

@Composable
fun FuelEfficiencyDashboard(
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    // UI 상태 관찰
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // 상태(uiState)에 따른 화면 분기
        when (val state = uiState) {
            is HomeState.Loading -> {
                // 1. 로딩 중일 때 표시할 UI
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.appColors.informativeActive
                    )
                }
            }


            is HomeState.WaitingForIgnition -> {
                // 🚗 시동 대기 상태: 시동 아이콘과 안내 문구
                StatusView(
                    icon = Icons.Default.PowerSettingsNew,
                    title = "시동 대기 중",
                    description = "연비 분석을 시작하려면\n차량의 시동을 걸어주세요.",
                    iconColor = MaterialTheme.colorScheme.outline
                )
            }

            is HomeState.Initializing -> {
                // 🔄 데이터 수집 중: 로딩 애니메이션과 분석 문구
                StatusView(
                    icon = Icons.Default.DirectionsCar, // 실제로는 showLoading이 우선됨
                    title = "데이터 분석 중",
                    description = "정확한 연비 계산을 위해\n주행 데이터를 수집하고 있습니다.",
                    showLoading = true
                )
            }

            is HomeState.Success -> {
                // 2. 데이터 로드 성공 시 표시할 UI
                SuccessContent(state.fuelEfficiency)
            }

            is HomeState.Error -> {
                // 3. 에러 발생 시 표시할 UI (필요에 따라 추가)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun SuccessContent(fuelEfficiency: Float) {
    // 1. 연비 등급 가져오기
    val grade = getFuelGrade(fuelEfficiency)
    val primaryColor = grade.color()

    val maxFuelEfficiency = 30f
    val progress = (fuelEfficiency / maxFuelEfficiency).coerceIn(0f, 1f)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            FuelGauge(
                progress = progress,
                primaryColor = primaryColor // 등급에 따른 색상
            )
            FuelInfo(
                fuelEfficiency = String.format("%.1f", fuelEfficiency),
                primaryColor = primaryColor, // 등급에 따른 색상
                grade = grade // 등급 정보 전달
            )
        }
    }
}

@Composable
fun FuelInfo(
    fuelEfficiency: String,
    primaryColor: Color,
    grade: FuelGrade // 추가
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocalGasStation,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(32.dp)
        )

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = fuelEfficiency,
                style = MaterialTheme.typography.displayLarge,
                color = primaryColor // 동적 색상
            )
            Text(
                text = " km/L",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // 연비 등급 표시 영역
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = grade.icon, // 등급별 아이콘
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
//                Text(
//                    text = stringResource(id = grade.titleRes), // "연비 상태"
//                    style = MaterialTheme.typography.labelMedium
//                )
                Text(
                    text = stringResource(id = grade.descriptionRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = primaryColor
                )
            }
        }
    }
}

@Composable
fun FuelGauge(
    modifier: Modifier = Modifier,
    progress: Float,
    primaryColor: Color
) {
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

@Composable
fun StatusView(
    icon: ImageVector,
    title: String,
    description: String,
    showLoading: Boolean = false,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = MaterialTheme.appColors.informativeActive,
                strokeWidth = 4.dp
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = iconColor
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
package com.example.fuelcompare.presentation.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fuelcompare.presentation.theme.appColors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import com.example.fuelcompare.R


@Composable
fun HomeScreen(navController: NavController) {
    FuelEfficiencyDashboard()
}

@Composable
fun FuelEfficiencyDashboard(
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        when (val state = uiState) {
            is HomeState.Loading -> {
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
                // 🎨 애니메이션이 적용된 StatusView
                StatusView(
                    icon = Icons.Default.PowerSettingsNew,
                    title = stringResource(R.string.home_waiting_title),
                    description = stringResource(R.string.home_waiting_desc),
                    iconColor = MaterialTheme.colorScheme.outline,
                    animateIcon = true
                )
            }

            is HomeState.Initializing -> {
                StatusView(
                    icon = Icons.Default.DirectionsCar,
                    title = stringResource(R.string.home_analyze_title),
                    description = stringResource(R.string.home_analyze_desc),
                    showLoading = true,
                    animateIcon = true
                )
            }

            is HomeState.Success -> {
                SuccessContent(state.fuelEfficiency)
            }

            is HomeState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is HomeState.TripEnd -> {
                TripSummaryContent(
                    summary = state.summary,
                )
            }
        }
    }
}

@Composable
fun SuccessContent(fuelEfficiency: Float) {
    val grade = getFuelGrade(fuelEfficiency)
    val primaryColor = grade.color()

    val maxFuelEfficiency = 30f
    val progress = (fuelEfficiency / maxFuelEfficiency).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            FuelGauge(
                progress = progress,
                primaryColor = primaryColor
            )
            FuelInfo(
                fuelEfficiency = String.format("%.1f", fuelEfficiency),
                primaryColor = primaryColor,
                grade = grade
            )
        }
    }
}

@Composable
fun FuelInfo(
    fuelEfficiency: String,
    primaryColor: Color,
    grade: FuelGrade
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 주유소 아이콘 크기 살짝 축소하여 상단 공간 확보
        Icon(
            imageVector = Icons.Default.LocalGasStation,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 🎨 타이포그래피 시인성 개선 (숫자는 더 크게, 단위는 연하게)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = fuelEfficiency,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 68.sp, // 메인 수치 극대화
                    fontWeight = FontWeight.Bold
                ),
                color = primaryColor
            )
            Text(
                text = "km/L",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, bottom = 14.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🎨 상태 메시지를 칩(Badge) 형태로 감싸서 세련되게 표현
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = primaryColor.copy(alpha = 0.1f), // 배경을 아주 옅은 메인 컬러로 설정
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = grade.icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(id = grade.descriptionRes),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
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
    val inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    val activeTickColor = Color.White.copy(alpha = 0.8f)

    val startAngle = 150f
    val sweepAngle = 240f
    val totalTicks = 50
    val strokeWidth = 45f

    Canvas(modifier = modifier.fillMaxSize(0.85f)) {
        val diameter = min(size.width, size.height)
        val arcSize = Size(diameter, diameter)
        val topLeft = Offset(
            (size.width - diameter) / 2,
            (size.height - diameter) / 2
        )

        // 배경 게이지 선
        drawArc(
            color = gaugeBackgroundColor.copy(alpha = 0.4f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 메인 게이지 선
        drawArc(
            color = primaryColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 눈금 그리기
        val arcRadius = diameter / 2
        for (i in 0..totalTicks) {
            val angleFraction = i.toFloat() / totalTicks
            val angleInDegrees = startAngle + angleFraction * sweepAngle
            val angleInRadians = Math.toRadians(angleInDegrees.toDouble()).toFloat()

            val isMajorTick = i % 5 == 0
            val tickLength = if (isMajorTick) strokeWidth + 10f else strokeWidth - 15f

            // 💡 미리 빼둔 색상 변수를 여기서 사용합니다.
            val tickColor = if (angleFraction <= progress) {
                activeTickColor
            } else {
                inactiveTickColor
            }

            val start = Offset(
                x = center.x + (arcRadius - tickLength / 2) * cos(angleInRadians),
                y = center.y + (arcRadius - tickLength / 2) * sin(angleInRadians)
            )
            val end = Offset(
                x = center.x + (arcRadius + tickLength / 2) * cos(angleInRadians),
                y = center.y + (arcRadius + tickLength / 2) * sin(angleInRadians)
            )

            drawLine(
                color = tickColor,
                start = start,
                end = end,
                strokeWidth = if (isMajorTick) 5f else 2.5f,
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
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    animateIcon: Boolean = false // 🎨 애니메이션 옵션 추가
) {
    // 무한 펄스(Pulse) 애니메이션 설정
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (showLoading) {
                // 로딩 애니메이션 (차량 아이콘을 감싸는 원형 진행률)
                CircularProgressIndicator(
                    modifier = Modifier.size(100.dp),
                    color = MaterialTheme.appColors.informativeActive.copy(alpha = 0.5f),
                    strokeWidth = 3.dp
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .scale(if (animateIcon) scale else 1f), // 애니메이션 적용
                tint = if (showLoading) MaterialTheme.appColors.informativeActive else iconColor
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
@Composable
fun TripSummaryContent(
    summary: com.example.domain.model.TripHistory,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {

            // 1. 상단 타이틀
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.trip_complete_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.trip_complete_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 2. 주요 지표 카드 레이아웃 (가로 배치)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 평균 연비 카드
                SummaryDataCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_avg_efficiency),
                    value = String.format("%.1f", summary.avgEfficiency),
                    unit = stringResource(R.string.unit_km_per_l),
                    icon = Icons.Default.LocalGasStation,
                    color = MaterialTheme.appColors.informativeActive
                )

                // 주행 거리 카드
                SummaryDataCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_total_distance),
                    value = String.format("%.1f", summary.totalDistance / 1000.0),
                    unit = stringResource(R.string.unit_km),
                    icon = Icons.Default.DirectionsCar,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 운전 습관 (급가감속 합산)
                val totalWarnings = summary.harshAccelCount + summary.harshBrakeCount
                SummaryDataCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_warning_drive),
                    value = "$totalWarnings",
                    unit = stringResource(R.string.unit_count),
                    icon = Icons.Default.Warning,
                    color = if (totalWarnings > 0)
                        MaterialTheme.appColors.regulationRed
                    else
                        MaterialTheme.colorScheme.onSurface,
                    isWarning = totalWarnings > 0
                )
            }
        }
    }
}

@Composable
fun SummaryDataCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color,
    isWarning: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) color.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isWarning) androidx.compose.foundation.BorderStroke(2.dp, color) else null
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    color = color
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

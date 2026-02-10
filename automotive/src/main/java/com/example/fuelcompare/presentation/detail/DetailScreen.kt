package com.example.fuelcompare.presentation.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fuelcompare.R
import com.example.fuelcompare.presentation.theme.appColors
import kotlin.math.roundToInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun DetailScreen(
    navController: NavController,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.record_screen_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                PeriodToggle(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelect = { viewModel.handleEvent(DetailEvent.ChangePeriod(it)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 메인 콘텐츠: 좌측(차트), 우측(상세정보) 2단 구성
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. 좌측 차트 영역
                Card(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    if (uiState.isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        FuelEfficiencyChart(
                            chartData = uiState.chartData,
                            selectedPoint = uiState.selectedPoint,
                            onPointSelect = { viewModel.handleEvent(DetailEvent.SelectPoint(it)) },
                            selectedPeriod = uiState.selectedPeriod
                        )
                    }
                }

                // 2. 우측 상세 정보 패널
                DrivingDetailPanel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    selectedPoint = uiState.selectedPoint,
                    selectedPeriod = uiState.selectedPeriod
                )
            }
        }
    }
}

@Composable
fun FuelEfficiencyChart(
    chartData: List<ChartPoint>,
    selectedPoint: ChartPoint?,
    onPointSelect: (ChartPoint) -> Unit,
    selectedPeriod: DisplayPeriod,
) {
    val chartColor = MaterialTheme.appColors.informativeActive
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val yMax = 30f
    val horizontalPadding = 16.dp

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding) // 💡 캔버스 내부 여백
                    .pointerInput(chartData) {
                        detectTapGestures { offset ->
                            val xStep = size.width / (chartData.size - 1)
                            val index =
                                (offset.x / xStep).roundToInt().coerceIn(0, chartData.size - 1)
                            onPointSelect(chartData[index])
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val xStep = width / (chartData.size - 1)

                // 1. Y축 가이드선 (더 은은하게)
                for (i in 0..3) {
                    val y = height - (height * (i * 10f / yMax))
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }

                // 2. 선택된 지점 수직 하이라이트
                selectedPoint?.let { pt ->
                    val index = chartData.indexOf(pt)
                    if (index != -1) {
                        drawLine(
                            color = chartColor.copy(alpha = 0.2f),
                            start = Offset(index * xStep, 0f),
                            end = Offset(index * xStep, height),
                            strokeWidth = 12.dp.toPx() // 두툼한 하이라이트
                        )
                    }
                }

                // 3. 데이터 선 (데이터가 없는 구간은 부드럽게 이어줌)
                val validPoints = chartData.mapIndexedNotNull { index, pt ->
                    pt.avgEfficiency?.let {
                        Offset(index * xStep, height - (height * (it / yMax).coerceIn(0f, 1f)))
                    }
                }

                if (validPoints.size > 1) {
                    val path = Path().apply {
                        moveTo(validPoints.first().x, validPoints.first().y)
                        validPoints.forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(
                        path,
                        color = chartColor,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 4. 점 그리기 (선택된 점은 더 크게)
                chartData.forEachIndexed { index, pt ->
                    pt.avgEfficiency?.let {
                        val center =
                            Offset(index * xStep, height - (height * (it / yMax).coerceIn(0f, 1f)))
                        val isSelected = pt == selectedPoint

                        drawCircle(
                            color = if (isSelected) Color.White else chartColor,
                            radius = if (isSelected) 8.dp.toPx() else 4.dp.toPx(),
                            center = center
                        )
                        if (isSelected) {
                            drawCircle(
                                color = chartColor,
                                radius = 8.dp.toPx(),
                                center = center,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
        ) {
            val fullWidth = maxWidth
            val labelIndices =
                if (chartData.size > 12) listOf(0, 7, 14, 21, 29) else chartData.indices.toList()

            labelIndices.forEach { index ->
                val xOffset = (fullWidth / (chartData.size - 1)) * index

                val labelText = remember(chartData[index].dateMillis, selectedPeriod) {
                    val date = java.util.Date(chartData[index].dateMillis)
                    if (selectedPeriod == DisplayPeriod.DAILY) {
                        java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault())
                            .format(date)
                    } else {
                        // 💡 줄바꿈 문제를 해결하기 위해 '26/01' 혹은 'Jan' 처럼 짧은 포맷 사용
                        java.text.SimpleDateFormat("yy/MM", java.util.Locale.getDefault())
                            .format(date)
                    }
                }

                Text(
                    text = labelText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .offset(x = xOffset - 20.dp)
                        .width(40.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DrivingDetailPanel(
    modifier: Modifier,
    selectedPoint: ChartPoint?,
    selectedPeriod: DisplayPeriod
) {

    val displayDate = remember(selectedPoint, selectedPeriod) {
        if (selectedPoint == null) return@remember ""

        val date = java.util.Date(selectedPoint.dateMillis)
        val pattern = if (selectedPeriod == DisplayPeriod.DAILY) {
            java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG)
        } else {
            java.text.SimpleDateFormat("yyyy MMMM", java.util.Locale.getDefault())
        }
        pattern.format(date)
    }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                alpha = 0.3f
            )
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = if (selectedPoint != null) displayDate else stringResource(R.string.instruction_choose_date),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

            if (selectedPoint?.avgEfficiency == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.desc_no_data),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // 연비 & 거리 (큼직하게)
                DetailItemRow(
                    label = stringResource(R.string.label_avg_eff),
                    value = stringResource(R.string.avg_efficiency, selectedPoint.avgEfficiency),
                    color = MaterialTheme.appColors.informativeActive
                )
                Spacer(modifier = Modifier.height(16.dp))
                DetailItemRow(
                    label = stringResource(R.string.label_mileage),
                    value = stringResource(R.string.avg_km, selectedPoint.totalDistance / 1000.0)
                )

                Spacer(modifier = Modifier.weight(1f))

                // 운전 습관 (급가감속)
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        HabitStat(
                            label = stringResource(R.string.label_harsh_accel),
                            count = selectedPoint.harshAccelCount,
                            selectedPeriod = selectedPeriod
                        )
                        HabitStat(
                            label = stringResource(R.string.label_harsh_break),
                            count = selectedPoint.harshBrakeCount,
                            selectedPeriod = selectedPeriod
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DetailItemRow(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(text = value, style = MaterialTheme.typography.headlineSmall, color = color)
    }
}

@Composable
fun HabitStat(
    label: String,
    count: Int,
    selectedPeriod: DisplayPeriod
) {
    val isNegative = count >= selectedPeriod.threshold

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = stringResource(R.string.format_count, count),
            style = MaterialTheme.typography.labelLarge,
            color = if (isNegative)
                MaterialTheme.appColors.informativeNegative
            else
                MaterialTheme.appColors.informativePositive
        )
    }
}

@Composable
fun PeriodToggle(selectedPeriod: DisplayPeriod, onPeriodSelect: (DisplayPeriod) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        listOf(DisplayPeriod.DAILY, DisplayPeriod.MONTHLY).forEach { period ->
            val isSelected = selectedPeriod == period
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onPeriodSelect(period) }
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(
                        if (period == DisplayPeriod.DAILY)
                            R.string.record_toggle_daily
                        else
                            R.string.record_toggle_monthly
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
package com.example.fuelcompare.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.TripStatistics
import com.example.domain.usecase.GetTripStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getTripStatisticsUseCase: GetTripStatisticsUseCase,
    private val reducer: DetailReducer
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailState())
    val uiState = _uiState.asStateFlow()

    private var statsJob: kotlinx.coroutines.Job? = null

    init {
        loadStats(DisplayPeriod.DAILY)
    }

    fun handleEvent(event: DetailEvent) {
        _uiState.value = reducer.reduce(_uiState.value, event)
        if (event is DetailEvent.ChangePeriod) {
            loadStats(event.period)
        }
    }

    private fun loadStats(period: DisplayPeriod) {
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            // 1. DB에서 데이터 가져오기
            getTripStatisticsUseCase(period == DisplayPeriod.MONTHLY)
                .flatMapLatest { stats ->
                    // 2  백그라운드 스레드에서 무거운 날짜 채우기 로직 실행
                    flow {
                        val processedData = withContext(Dispatchers.Default) {
                            fillMissingDates(stats, period)
                        }
                        emit(processedData)
                    }
                }
                .collect { filledData ->
                    handleEvent(DetailEvent.StatsLoaded(filledData))
                }
        }
    }

    private fun fillMissingDates(stats: List<TripStatistics>, period: DisplayPeriod): List<ChartPoint> {
        val statsMap = stats.associateBy { stat ->
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = stat.timestamp }
            if (period == DisplayPeriod.DAILY) {
                "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.DAY_OF_YEAR)}"
            } else {
                "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.MONTH)}"
            }
        }

        val calendar = java.util.Calendar.getInstance()
        val result = mutableListOf<ChartPoint>()
        val range = if (period == DisplayPeriod.DAILY) 29 else 11

        for (i in range downTo 0) {
            val tempCal = calendar.clone() as java.util.Calendar
            if (period == DisplayPeriod.DAILY) {
                tempCal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            } else {
                tempCal.add(java.util.Calendar.MONTH, -i)
                tempCal.set(java.util.Calendar.DAY_OF_MONTH, 1)
            }

            // 검색 키 생성
            val key = if (period == DisplayPeriod.DAILY) {
                "${tempCal.get(java.util.Calendar.YEAR)}-${tempCal.get(java.util.Calendar.DAY_OF_YEAR)}"
            } else {
                "${tempCal.get(java.util.Calendar.YEAR)}-${tempCal.get(java.util.Calendar.MONTH)}"
            }

            val match = statsMap[key] // O(1) 검색

            result.add(ChartPoint(
                dateMillis = tempCal.timeInMillis,
                avgEfficiency = match?.avgEfficiency,
                harshAccelCount = match?.harshAccelCount ?: 0,
                harshBrakeCount = match?.harshBrakeCount ?: 0,
                totalDistance = match?.totalDistance ?: 0.0,
            ))
        }
        return result
    }
}
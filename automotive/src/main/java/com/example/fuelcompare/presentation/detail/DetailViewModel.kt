package com.example.fuelcompare.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.TripStatistics
import com.example.domain.usecase.GetTripStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getTripStatisticsUseCase: GetTripStatisticsUseCase,
    private val reducer: DetailReducer
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailState())
    val uiState = _uiState.asStateFlow()

    private var statsJob: Job? = null

    init {
        loadStats(DisplayPeriod.DAILY)
    }

    fun handleEvent(event: DetailEvent) {
        _uiState.value = reducer.reduce(_uiState.value, event)
        if (event is DetailEvent.ChangePeriod) {
            loadStats(event.period)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadStats(period: DisplayPeriod) {
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            getTripStatisticsUseCase(period == DisplayPeriod.MONTHLY)
                .mapLatest { stats ->
                    fillMissingDates(stats, period)
                }
                .flowOn(Dispatchers.Default)
                .collect { filledData ->
                    handleEvent(DetailEvent.StatsLoaded(filledData))
                }
        }
    }


    private fun fillMissingDates(stats: List<TripStatistics>, period: DisplayPeriod): List<ChartPoint> {
        val statsMap = stats.associateBy { stat ->
            val cal = Calendar.getInstance().apply { timeInMillis = stat.timestamp }
            if (period == DisplayPeriod.DAILY) {
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
            } else {
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
            }
        }

        val calendar = Calendar.getInstance()
        val result = mutableListOf<ChartPoint>()
        val range = if (period == DisplayPeriod.DAILY) 29 else 11

        for (i in range downTo 0) {
            val tempCal = calendar.clone() as Calendar
            if (period == DisplayPeriod.DAILY) {
                tempCal.add(Calendar.DAY_OF_YEAR, -i)
            } else {
                tempCal.add(Calendar.MONTH, -i)
                tempCal.set(Calendar.DAY_OF_MONTH, 1)
            }

            // 검색 키 생성
            val key = if (period == DisplayPeriod.DAILY) {
                "${tempCal.get(Calendar.YEAR)}-${tempCal.get(Calendar.DAY_OF_YEAR)}"
            } else {
                "${tempCal.get(Calendar.YEAR)}-${tempCal.get(Calendar.MONTH)}"
            }

            val match = statsMap[key]

            result.add(ChartPoint(
                dateMillis = tempCal.timeInMillis,
                avgEfficiency = match?.avgEfficiency,
                harshAccelCount = match?.harshAccelCount ?: 0,
                harshBrakeCount = match?.harshBrakeCount ?: 0,
                totalDistance = match?.totalDistance ?: 0.0,
                totalDuration = match?.tripDuration ?: 0L,
                idlingDuration = match?.idlingTimeMillis ?:0L,
                cruiseDuration = match?.cruiseTimeMillis ?:0L,
                coastingDuration = match?.coastingTimeMillis ?:0L,
            ))
        }
        return result
    }
}
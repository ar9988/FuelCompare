package com.example.domain.usecase

import com.example.domain.repository.HistoryRepository
import javax.inject.Inject

class RecordDrivingEventUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    private var accelCount = 0
    private var brakeCount = 0

    fun incrementAccel() { accelCount++ }
    fun incrementBrake() { brakeCount++ }

    fun getCounts() = Pair(accelCount, brakeCount)

    fun reset() {
        accelCount = 0
        brakeCount = 0
    }
}
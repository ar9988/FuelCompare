package com.example.domain.usecase

import com.example.domain.model.TripHistory
import com.example.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTripHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
){
    operator fun invoke() : Flow<List<TripHistory>>{
        return historyRepository.getAllHistory()
    }
}
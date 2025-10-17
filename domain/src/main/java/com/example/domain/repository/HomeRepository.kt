package com.example.domain.repository

import com.example.domain.model.VehicleStatus
import kotlinx.coroutines.flow.Flow

interface HomeRepository {

    fun observeVehicleStatus(): Flow<VehicleStatus>
}
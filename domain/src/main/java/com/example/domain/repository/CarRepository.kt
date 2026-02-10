package com.example.domain.repository

import com.example.domain.model.VehicleGearState
import com.example.domain.model.VehicleIgnitionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CarRepository {
    val ignitionState: StateFlow<VehicleIgnitionState>

    val fuelEfficiency: StateFlow<Float>

    val gearState: StateFlow<VehicleGearState>

    fun observeSpeed(): Flow<Float>

    fun observeFuelLevel(): Flow<Float>

    fun getEfficiency(): Flow<Float>

    fun observeEngineRpm(): Flow<Float>

    fun getTotalDistance(): Double
}
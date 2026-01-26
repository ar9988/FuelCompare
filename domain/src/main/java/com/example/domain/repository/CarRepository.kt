package com.example.domain.repository

import com.example.domain.model.VehicleIgnitionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CarRepository {
    val ignitionState: StateFlow<VehicleIgnitionState>

    val fuelEfficiency: StateFlow<Float>

    fun observeSpeed(): Flow<Float>

    fun observeFuelLevel(): Flow<Float>

    fun getEfficiency(): Flow<Float>

    fun observeEngineRpm(): Flow<Float>

    fun observeGear(): Flow<Int>

    fun getTotalDistance(): Double
}
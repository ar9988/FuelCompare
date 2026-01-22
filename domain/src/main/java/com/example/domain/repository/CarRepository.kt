package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface CarRepository {
    fun observeSpeed(): Flow<Float>

    fun observeFuelLevel(): Flow<Float>

    fun getEfficiency(): Flow<Float>

    fun observeEngineRpm(): Flow<Float>

    fun observeGear(): Flow<Int>
}
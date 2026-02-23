package com.example.fuelcompare.presentation.home

import com.example.domain.model.FuelEfficiencyState
import com.example.domain.model.TripState
import javax.inject.Inject

class HomeReducer @Inject constructor() {
    // HomeReducer.kt
    fun reduce(homeState: HomeState, homeEvent: HomeEvent): HomeState {
        return when (homeEvent) {
            is HomeEvent.UpdateTripState -> {
                when (homeEvent.trip) {
                    // 주차 중이거나 대기 중일 때
                    is TripState.Idle ->
                        return HomeState.WaitingForIgnition

                    // 주행이 막 시작되었을 때 (P -> D)
                    is TripState.Driving ->
                        return HomeState.Initializing

                    // 주행이 끝났을 때 (D -> P)
                    is TripState.Finished -> {
                        return HomeState.TripEnd(homeEvent.trip.summary)
                    }
                }
            }

            is HomeEvent.UpdateData -> {
                if (homeState is HomeState.TripEnd ||homeState is HomeState.WaitingForIgnition) return homeState

                if(homeEvent.fuelEfficiency is FuelEfficiencyState.Ready){
                    val fuelEff = homeEvent.fuelEfficiency.efficiency
                    HomeState.Success(
                        fuelEff,
                        grade = getFuelGrade(fuelEff)
                    )
                }
                else{
                    homeState
                }
            }
        }
    }
}
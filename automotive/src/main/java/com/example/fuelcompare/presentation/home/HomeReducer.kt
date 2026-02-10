package com.example.fuelcompare.presentation.home

import android.util.Log
import com.example.domain.model.VehicleGearState
import javax.inject.Inject

class HomeReducer @Inject constructor() {
    // HomeReducer.kt
    fun reduce(homeState: HomeState, homeEvent: HomeEvent): HomeState {
        return when (homeEvent) {
            is HomeEvent.UpdateGearState -> {
                if (homeEvent.gear == VehicleGearState.PARK || homeEvent.gear == VehicleGearState.UNDEFINED) {
                    HomeState.WaitingForIgnition
                } else {
                    // 기어가 DRIVE 등으로 바뀌면 '데이터 수집 중'으로 일단 변경
                    if (homeState is HomeState.WaitingForIgnition || homeState is HomeState.Loading) {
                        HomeState.Initializing
                    } else homeState
                }
            }

            is HomeEvent.UpdateData -> {
                // 시동 대기 중이면 무시
                if (homeState is HomeState.WaitingForIgnition) return homeState

                // 💡 핵심 수정: 0.0f 라도 데이터가 들어왔다면 바로 Success 화면으로 전환합니다.
                // 더 이상 Initializing에 가두지 않습니다.
                HomeState.Success(
                    fuelEfficiency = homeEvent.fuelEfficiency,
                    grade = getFuelGrade(homeEvent.fuelEfficiency)
                )
            }
        }
    }
}
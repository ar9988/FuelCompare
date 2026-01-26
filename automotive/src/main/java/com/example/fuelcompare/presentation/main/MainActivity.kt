package com.example.fuelcompare.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.setContent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.example.fuelcompare.presentation.theme.MyAppTheme
import dagger.hilt.android.AndroidEntryPoint

// MainActivity.kt

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: DrivingAssistantViewModel by viewModels()

    // 1. 여러 권한을 요청할 수 있는 런처로 변경
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val speedGranted = permissions["android.car.permission.CAR_SPEED"] ?: false
        val energyGranted = permissions["android.car.permission.CAR_ENERGY"] ?: false

        if (audioGranted && speedGranted && energyGranted) {
            // 모든 권한이 승인됨 -> 관찰 및 서비스 시작
            Log.d("Permission", "모든 권한 승인 완료")
            // 필요 시 viewModel에 신호 전달
        } else {
            // 하나라도 거부됨
            Log.e("Permission", "일부 권한이 거부되었습니다.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. 앱 실행 시 또는 필요 시점에 권한 요청
        checkAndRequestVehiclePermissions()

        setContent {
            val mainState by mainViewModel.uiState.collectAsState()
            MyAppTheme {
                AppRoot(
                    isListening = mainState.isListening,
                    onListeningButtonClick = {
                        mainViewModel.handleIntent(MainEvent.StartListening)
                    }
                )
            }
        }
    }

    private fun checkAndRequestVehiclePermissions() {
        val permissionsToRequest = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            "android.car.permission.CAR_SPEED",
            "android.car.permission.CAR_ENERGY"
        )

        // 권한이 하나라도 승인되지 않았다면 팝업 요청
        val isAllGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!isAllGranted) {
            requestPermissionsLauncher.launch(permissionsToRequest)
        }
    }
}
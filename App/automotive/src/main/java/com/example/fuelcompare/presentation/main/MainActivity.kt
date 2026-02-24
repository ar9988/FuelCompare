package com.example.fuelcompare.presentation.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.fuelcompare.R
import com.example.fuelcompare.presentation.theme.MyAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val drivingAssistantViewModel: DrivingAssistantViewModel by viewModels()

    // 💡 권한 승인 후 재시작 다이얼로그를 띄우기 위한 상태 변수
    private var showRestartDialog by mutableStateOf(false)

    // 1. 여러 권한을 요청할 수 있는 런처
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // 요청한 모든 권한이 true(승인)인지 확인
        val allGranted = permissions.entries.all { it.value }

        if (allGranted) {
            Log.d("Permission", "✅ 모든 권한 승인 완료 -> 재시작 다이얼로그 노출")
            // 권한이 방금 승인되었으므로, 센서 재등록을 위해 재시작 안내 팝업을 띄웁니다.
            showRestartDialog = true
        } else {
            Log.e("Permission", "❌ 일부 권한이 거부되었습니다.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. 앱 실행 시 권한 요청 (이미 다 승인되어 있다면 런처가 실행되지 않음)
        checkAndRequestVehiclePermissions()

        setContent {
            val mainState by drivingAssistantViewModel.uiState.collectAsState()

            val navController = rememberNavController()
            LaunchedEffect(Unit) {
                drivingAssistantViewModel.sideEffect.collect { effect ->
                    when (effect) {
                        is MainSideEffect.NavigateToSummary -> {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    }
                }
            }
            MyAppTheme {
                AppRoot(
                    navController = navController,
                    isListening = mainState.isListening,
                    onListeningButtonClick = {
                        drivingAssistantViewModel.handleIntent(MainEvent.StartListening)
                    }
                )

                // 💡 최초 권한 승인 시 나타나는 재시작 팝업
                if (showRestartDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            // 배경 터치로 닫히는 것을 방지 (무조건 재시작해야 함)
                        },
                        title = { Text(stringResource(R.string.permission_title)) },
                        text = { Text(stringResource(R.string.permission_desc)) },
                        confirmButton = {
                            Button(onClick = { restartApp() }) {
                                Text(stringResource(R.string.restart_desc))
                            }
                        }
                    )
                }
            }
        }
    }

    private fun checkAndRequestVehiclePermissions() {
        // 💡 RPM, 기어 권한 포함 모든 필수 권한 명시
        val permissionsToRequest = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            "android.car.permission.CAR_SPEED",
            "android.car.permission.CAR_ENERGY",
            "android.car.permission.CAR_POWERTRAIN",      // 기어(GEAR_SELECTION) 읽기
            "android.car.permission.CAR_EXTERIOR_ENVIRONMENT" // 엔진(ENGINE_RPM) 역할을 하는 외부 온도 읽기
        )

        val isAllGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!isAllGranted) {
            requestPermissionsLauncher.launch(permissionsToRequest)
        }
    }

    // 💡 앱을 완전히 죽이고 다시 띄우는 강력한 재시작 함수
    private fun restartApp() {
        Log.d("System", "🔄 앱 재시작 수행")
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        finish()
        Runtime.getRuntime().exit(0)
    }
}
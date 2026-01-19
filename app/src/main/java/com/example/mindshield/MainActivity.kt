package com.example.mindshield

import ScreenAdapter
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mindshield.data.repository.OnboardingManager
import com.example.mindshield.domain.calibration.BaselineStorage
import com.example.mindshield.domain.calibration.UserBaseline
import com.example.mindshield.ui.MainScreen
import com.example.mindshield.ui.theme.MindShieldTheme
import com.example.mindshield.ui.viewmodel.OnboardingScreenViewModel
import kotlinx.coroutines.launch
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mindshield.data.preferences.UserSettings
import com.example.mindshield.ui.screens.CalibrationScreen
import com.example.mindshield.ui.screens.OnboardingScreen
import com.example.mindshield.ui.viewmodel.InterventionScreenViewModel
import com.example.mindshield.ui.viewmodel.InterventionScreenViewModelFactory
import kotlinx.coroutines.flow.first


class MainActivity : ComponentActivity() {

    private val onboardingScreenViewModel: OnboardingScreenViewModel by viewModels()

    private lateinit var userSettings: UserSettings
    private val interventionScreenViewModel: InterventionScreenViewModel by viewModels {
        InterventionScreenViewModelFactory(userSettings)
    }

    // 初始化 OnboardingManager
    private lateinit var onboardingManager: OnboardingManager

    // 控制界面显示的 State: null=加载中, true=主页, false=引导页
    private var showMainScreenState by mutableStateOf<Boolean?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userSettings = UserSettings(applicationContext)

        onboardingManager = OnboardingManager(this)

        lifecycleScope.launch {
            val isCompleted = onboardingManager
                .isOnboardingCompleted
                .first()
            showMainScreenState = isCompleted
        }

        val storage = BaselineStorage(this)
        storage.loadBaseline()

        println("=== Formatted UserBaseline Data ===")

        val metrics = listOf(
            "HR" to UserBaseline.hr,
            "RMSSD" to UserBaseline.rmssd,
            "SDNN" to UserBaseline.sdnn,
            "pNN50" to UserBaseline.pnn50,
            "LF" to UserBaseline.lf,
            "HF" to UserBaseline.hf
        )

        metrics.forEach { (name, stat) ->
            println("$name: ${stat.mean} ± ${stat.stdDev}")
        }
        println("Calibrated: ${UserBaseline.isCalibrated}")

        checkPermissionsAndStart()

        setContent {
            BoxWithConstraints {
                ScreenAdapter(actualWidth = maxWidth) {
                    val navController = rememberNavController()
                    // 打开时执行一次
                    val startDest = if (showMainScreenState == true) "main" else "onboarding"
                    MindShieldTheme {
                        // 重复执行
                        NavHost(
                            navController = navController,
                            startDestination = startDest // 动态决定起始页
                        ){
                            // === 页面 A: 引导页 (Onboarding) ===
                            composable("onboarding") {
                                OnboardingScreen(
                                    viewModel = onboardingScreenViewModel,
                                    onFinish = {
                                        // 完成引导 -> 去主页
                                        lifecycleScope.launch {
                                            onboardingManager.completeOnboarding()
                                        }
                                        // 【关键】跳转到 main，并把 onboarding 从后退栈里移除
                                        // 这样用户在主页按返回键不会回到引导页
                                        navController.navigate("main") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // === 页面 B: 主页 (Main) ===
                            composable("main") {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    MainScreen(
                                        // 动作 1: 去校准页 (进入下一级)
                                        interventionScreenViewModel,
                                        onNavigateToCalibration = {
                                            navController.navigate("calibration")
                                        }
                                    )

                                }
                            }

                            // === 页面 C: 校准页 (Calibration) ===
                            composable("calibration") {
                                CalibrationScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onRetestClick = { navController.navigate("onboarding") },
                                    onClearClick = {
                                        storage.clearBaseline()
                                        UserBaseline.reset()
                                    }
                                )
                            }
                        }

                    }
                }
            }

        }
    }

    private val requiredPermissions: Array<String>
        get() {
            val permissions = mutableListOf<String>()

            // Android 12+ (API 31+) needs specific Bluetooth permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                // Android 11 and below needs Location to use Bluetooth
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
                permissions.add(Manifest.permission.BLUETOOTH)
                permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
            }

            // Android 13+ (API 33+) needs Notification permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            return permissions.toTypedArray()
        }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if all permissions were granted
        val allGranted = permissions.entries.all { it.value }

        if (allGranted) {
            // Success! User said yes. Start the engine.
            startMonitoringService()
        } else {
            // Failure. User said no.
            // You should show a UI dialog explaining why you need them.
            // For now, we just do nothing.

            // [可选] 即使拒绝了权限，通常也应该检查引导状态，避免卡在白屏
            // checkOnboardingStatus()
        }
    }

    private fun checkPermissionsAndStart() {
        // Filter out permissions we don't have yet
        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            // We already have everything (e.g., 2nd time opening app)
            startMonitoringService()
        } else {
            // We are missing some. Show the popup.
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun startMonitoringService() {
        val intent = Intent(this, com.example.mindshield.service.MindShieldService::class.java)

        // Check version because startForegroundService is required for newer Androids
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        // 服务启动（意味着权限已就绪）后，开始检查引导状态
        checkOnboardingStatus()
    }

    // 辅助函数：读取 DataStore 决定显示哪个 UI
    private fun checkOnboardingStatus() {
        lifecycleScope.launch {
            onboardingManager.isOnboardingCompleted.collect { completed ->
                showMainScreenState = completed
            }
        }
    }

}
package com.example.mindshield

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mindshield.data.repository.OnboardingManager
import com.example.mindshield.ui.MainScreen
import com.example.mindshield.ui.Screens.OnboardingScreen
import com.example.mindshield.ui.theme.MindShieldTheme
import com.example.mindshield.ui.theme.*
import com.example.mindshield.ui.viewmodel.StartScreenViewModel
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val viewModel: StartScreenViewModel by viewModels()

    // 初始化 OnboardingManager
    private lateinit var onboardingManager: OnboardingManager

    // 控制界面显示的 State: null=加载中, true=主页, false=引导页
    private var showMainScreenState by mutableStateOf<Boolean?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 Manager
        onboardingManager = OnboardingManager(this)

        checkPermissionsAndStart()

        setContent {
            MindShieldTheme {
                // 根据状态决定显示哪个界面
                if (showMainScreenState == true) {
                    // === 原有代码逻辑开始 (只有是主页时才显示) ===
                    Box(modifier = Modifier.fillMaxSize()){
                        MainScreen(viewModel = viewModel)
                        FloatingBox(
                            modifier = Modifier
                                .align(alignment = Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    // === 原有代码逻辑结束 ===
                } else if (showMainScreenState == false) {
                    // 显示引导页
                    OnboardingScreen(
                        onFinish = {
                            // 用户完成引导，更新 DataStore 并切换到主页
                            lifecycleScope.launch {
                                onboardingManager.completeOnboarding()
                                showMainScreenState = true
                            }
                        }
                    )
                } else {
                    // showMainScreenState == null
                    // 权限检查中或数据读取中，可以留空或显示个 Loading
                }
            }
        }
    }

    @Composable
    fun FloatingBox(modifier: Modifier = Modifier) {
        val showFloatingBox by viewModel.showFloatingBox.collectAsState()
        val seconds by viewModel.countdown.collectAsState()
        AnimatedVisibility(
            visible = showFloatingBox,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            Box(
                modifier = modifier
                    .size(64.dp)
                    .background(White40, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("${if (seconds != null) seconds else "-"}", color = Stone800)
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
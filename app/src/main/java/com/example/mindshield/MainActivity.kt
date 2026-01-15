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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.mindshield.ui.MainScreen
import com.example.mindshield.ui.theme.MindShieldTheme
import com.example.mindshield.ui.theme.*
import com.example.mindshield.ui.viewmodel.StartScreenViewModel


class MainActivity : ComponentActivity() {

    private val viewModel: StartScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissionsAndStart()
        setContent {
            MindShieldTheme {
                Box(modifier = Modifier.fillMaxSize()){
                    MainScreen(viewModel = viewModel)
                    FloatingBox(
                        modifier = Modifier
                            .align(alignment = Alignment.Center)
                            .padding(16.dp)
                    )
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
    }
}
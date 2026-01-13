package com.example.mindshield

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.mindshield.service.MindShieldService
import com.example.mindshield.ui.MainScreen
import com.example.mindshield.ui.theme.MindShieldTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissionsAndStart()
        setContent {
            MindShieldTheme {
                MainScreen()
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
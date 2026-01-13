package com.example.mindshield

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.mindshield.service.MindShieldService
import com.example.mindshield.ui.MainScreen
import com.example.mindshield.ui.theme.MindShieldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MindShieldTheme {
                MainScreen()

            }
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
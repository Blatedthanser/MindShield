package com.example.mindshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
}
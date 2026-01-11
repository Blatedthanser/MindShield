package com.example.mindshield.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    background = BeigeBackground,
    surface = BeigeBackground,
    onSurface = Stone900,
    primary = Emerald700
)

@Composable
fun MindShieldTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
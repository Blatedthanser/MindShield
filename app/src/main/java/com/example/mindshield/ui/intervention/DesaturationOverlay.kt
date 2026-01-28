package com.example.mindshield.ui.intervention

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay


val WashoutGray = Color(0xFF808080)

@Composable
fun DesaturationOverlay(
    durationMillis: Int,
    onFinished: () -> Unit
) {
    val targetAlpha = 0.3f
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(targetAlpha, tween(5_000))

        val holdTime = if (durationMillis > 3000) durationMillis - 3000 else 1000
        delay(holdTime.toLong())

        alphaAnim.animateTo(0f, tween(1500))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WashoutGray.copy(alpha = alphaAnim.value))
    )
}
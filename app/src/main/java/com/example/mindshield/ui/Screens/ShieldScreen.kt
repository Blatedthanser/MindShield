package com.example.mindshield.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindshield.model.StressLevel
import com.example.mindshield.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ShieldScreen() {
    var heartRate by remember { mutableIntStateOf(72) }
    var stressLevel by remember { mutableStateOf(StressLevel.CALM) }

    // 模拟心率循环
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            val change = (-2..2).random()
            heartRate = (heartRate + change).coerceIn(60, 130)
            stressLevel = when {
                heartRate > 100 -> StressLevel.HIGH
                heartRate > 85 -> StressLevel.MODERATE
                else -> StressLevel.CALM
            }
        }
    }

    // 呼吸动画
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    val themeColor = when (stressLevel) {
        StressLevel.HIGH -> Orange600
        StressLevel.MODERATE -> Color(0xFFB45309)
        StressLevel.CALM -> Emerald700
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Watch, null, Modifier.size(14.dp), tint = themeColor)
                Spacer(Modifier.width(4.dp))
                Text("Connected", fontSize = 12.sp, color = Stone600)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.BatteryStd, null, Modifier.size(14.dp), tint = Stone600)
                Text("84%", fontSize = 12.sp, color = Stone600, modifier = Modifier.padding(horizontal = 4.dp))
                Icon(Icons.Outlined.Lock, null, Modifier.size(14.dp), tint = Emerald700)
            }
        }

        // Center Content (Circle + Card grouped together to be centered vertically)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Breathing Circle
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier
                    .size(300.dp)
                    .background(Brush.radialGradient(listOf(themeColor.copy(0.2f), Color.Transparent))))
                Box(modifier = Modifier
                    .size(256.dp)
                    .scale(scale)
                    .border(2.dp, themeColor.copy(0.3f), CircleShape))
                Box(modifier = Modifier
                    .size(192.dp)
                    .border(4.dp, themeColor.copy(0.4f), CircleShape))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.MonitorHeart, null, tint = themeColor, modifier = Modifier.size(32.dp))
                    Text("$heartRate", fontSize = 48.sp, color = Stone800, fontWeight = FontWeight.Light)
                    Text("BPM", fontSize = 14.sp, color = Stone500)
                    Text(
                        text = if (stressLevel == StressLevel.HIGH) "HIGH STRESS" else "RESILIENT",
                        fontSize = 12.sp,
                        color = themeColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Bottom Card
            Card(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White40),
                border = BorderStroke(1.dp, Color.White.copy(0.5f))
            ) {
                Column(Modifier.padding(20.dp)) {
                    // Title Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(themeColor.copy(0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.VerifiedUser, null, tint = themeColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("MindShield Active", color = Stone800, fontWeight = FontWeight.Medium)
                            Text("Monitoring emotional fluctuations...", color = Stone600, fontSize = 12.sp)
                        }
                    }

                    // Divider
                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Stone300.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Interventions Today", color = Stone600, fontSize = 14.sp)
                        Text("3", color = Stone800, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Bottom Spacer to lift content slightly above nav bar area if needed
        Spacer(modifier = Modifier.height(16.dp))
    }
}
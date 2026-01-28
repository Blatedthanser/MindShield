package com.example.mindshield.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import com.example.mindshield.data.repository.DynamicDataToShieldPage
import com.example.mindshield.data.repository.InterventionRepository
import com.example.mindshield.data.source.WearableSimulator
import com.example.mindshield.domain.analysis.MentalState.*
import com.example.mindshield.model.InterventionEvent
import com.example.mindshield.ui.theme.*
import f
import w
import java.util.Calendar
@Composable
fun ShieldScreen() {

    val eventsList by InterventionRepository.events.collectAsState(initial = emptyList())

    val liveHr by DynamicDataToShieldPage.currentHr.collectAsState()

    val state by DynamicDataToShieldPage.currentState.collectAsState()
    // 呼吸动画
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    val themeColor = when (state) {
        CALM_OR_HAPPY -> Emerald600
        EXCITEMENT -> Emerald800
        DISTRESS -> Orange600
        NULL -> Emerald600
    }

    val animatedColor by animateColorAsState(
        targetValue = themeColor,
        animationSpec = tween(durationMillis = 1500),
        label = "ColorAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 20.w)
            .background(BeigeBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.w),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Watch, null, Modifier.size(14.w), tint = if (WearableSimulator.isConnected) animatedColor else Stone600)
                Spacer(Modifier.width(4.w))
                Text(if (WearableSimulator.isConnected) "Connected" else "Not Connected", fontSize = 12.f, color = Stone600)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.BatteryStd, null, Modifier.size(14.w), tint = Stone600)
                Text("84%", fontSize = 12.f, color = Stone600, modifier = Modifier.padding(horizontal = 4.w))
                Icon(Icons.Outlined.Lock, null, Modifier.size(14.w), tint = Emerald700)
            }
        }

        // Center Content (Circle + Card grouped together to be centered vertically)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Breathing Circle
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier
                    .size(300.w)
                    .background(Brush.radialGradient(listOf(animatedColor.copy(0.2f), Color.Transparent))))
                Box(modifier = Modifier
                    .size(256.w)
                    .scale(scale)
                    .border(2.w, animatedColor.copy(0.3f), CircleShape))
                Box(modifier = Modifier
                    .size(192.w)
                    .border(4.w, animatedColor.copy(0.4f), CircleShape))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.MonitorHeart, null, tint = themeColor, modifier = Modifier.size(32.w))
                    Text("${if (liveHr == 0) "--" else liveHr}", fontSize = 48.f, color = Stone800, fontWeight = FontWeight.Light)
                    Text("BPM", fontSize = 14.f, color = Stone500)
                    Text(
                        text = when  {
                            liveHr > 115 -> "HIGH"
                            liveHr > 70 -> "MODERATE"
                            else -> "LOW"
                        },
                        fontSize = 12.f,
                        color = animatedColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.f,
                        modifier = Modifier.padding(top = 8.w)
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.w))

            // Bottom Card
            Card(
                modifier = Modifier
                    .padding(horizontal = 24.w)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = animatedColor.copy(alpha = 0.1f).compositeOver(BeigeBackground)),
                border = BorderStroke(1.w, Color.White.copy(0.5f))

            ){

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.w),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("HRV-Based Mood Status", color = Stone600, fontWeight = FontWeight.Medium, fontSize = 12.f)
                    Spacer(modifier = Modifier.height(8.w))
                    Text(
                        text = when (state) {
                            CALM_OR_HAPPY -> "CALM OR HAPPY"
                            EXCITEMENT -> "EXCITEMENT"
                            DISTRESS -> "DISTRESS"
                            NULL -> "ANALIZING..."
                        },
                        color = Stone800,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.f
                    )
                    Spacer(modifier = Modifier.height(8.w))
                }
            }

            Spacer(modifier = Modifier.height(24.w))

            Card(
                modifier = Modifier
                    .padding(horizontal = 24.w)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White40),
                border = BorderStroke(1.w, Color.White.copy(0.5f))
            ) {
                Column(Modifier.padding(20.w)) {
                    // Title Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.w)
                                .background(animatedColor.copy(0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.VerifiedUser, null, tint = themeColor, modifier = Modifier.size(20.w))
                        }
                        Spacer(Modifier.width(12.w))
                        Column(verticalArrangement = Arrangement.spacedBy(4.w)) {
                            Text("MindShield Active", color = Stone800, fontSize = 16.f, fontWeight = FontWeight.Medium, lineHeight = 22.f)
                            Text("Monitoring emotional fluctuations...", color = Stone600, fontSize = 12.f, lineHeight = 18.f)
                        }
                    }

                    // Divider
                    Divider(
                        modifier = Modifier.padding(vertical = 16.w),
                        color = Stone300.copy(alpha = 0.5f),
                        thickness = 1.w
                    )
                    val todayCount = countTodayInterventions(eventsList)
                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Interventions Today", color = Stone600, fontSize = 14.f)
                        Text("$todayCount", color = Stone800, fontWeight = FontWeight.Bold, fontSize = 14.f)
                    }
                }
            }
        }

        // Bottom Spacer to lift content slightly above nav bar area if needed
        Spacer(modifier = Modifier.height(16.w))
    }
}


fun countTodayInterventions(events: List<InterventionEvent>): Int {
    val calendar = Calendar.getInstance()

    // 获取今天的年月日
    val todayYear = calendar.get(Calendar.YEAR)
    val todayMonth = calendar.get(Calendar.MONTH)
    val todayDay = calendar.get(Calendar.DAY_OF_MONTH)

    var count = 0
    events.forEach { event ->
        calendar.timeInMillis = event.timestamp
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        if (year == todayYear && month == todayMonth && day == todayDay) {
            count++
        }
    }

    return count
}
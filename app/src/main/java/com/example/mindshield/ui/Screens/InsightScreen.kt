package com.example.mindshield.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindshield.data.*
import com.example.mindshield.ui.theme.*

@Composable
fun InsightScreen() {
    var expandedEventId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BeigeBackground),
        contentPadding = PaddingValues(24.dp)
    ) {
        // --- Page Title ---
        item {
            Text(
                text = "Insight",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Stone800,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // --- 1. Stress Time Distribution Chart ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDCF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .border(1.dp, Stone300, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    // Chart Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF4F46E5)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Stress Time Distribution", fontSize = 14.sp, color = Stone600)
                    }
                    Spacer(Modifier.height(16.dp))

                    // Bar Chart Rendering
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        HOURLY_STRESS_DATA.forEach { data ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height((data.value * 15).dp)
                                        .background(
                                            if (data.value > 6) Orange600 else Color(0xFFA8A29E),
                                            RoundedCornerShape(4.dp, 4.dp, 0.dp, 0.dp)
                                        )
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(data.name, fontSize = 10.sp, color = Stone600)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // --- 2. Most Irritable Apps Ranking (New Section) ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDCF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Stone300, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Section Header
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Orange600
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Most Irritable Apps",
                            fontSize = 14.sp,
                            color = Stone600
                        )
                    }

                    // Ranking List
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        APP_RANKING_DATA.forEachIndexed { index, item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Rank Number (e.g., 01, 02)
                                Text(
                                    text = "0${index + 1}",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Stone500,
                                    modifier = Modifier.width(28.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    // App Name & Percentage
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(item.name, fontSize = 12.sp, color = Stone900)
                                        Text("${item.value}%", fontSize = 12.sp, color = Stone500)
                                    }

                                    // Progress Bar Track
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Stone300)
                                    ) {
                                        // Progress Bar Fill
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(item.value / 100f)
                                                .height(6.dp)
                                                .background(
                                                    color = Orange600.copy(alpha = 1f - (index * 0.2f)),
                                                    shape = RoundedCornerShape(3.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // --- 3. Emotional Timeline Header ---
        item {
            Text(
                text = "Emotional Timeline",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Stone800,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // --- 4. Timeline Events ---
        items(MOCK_EVENTS) { event ->
            val isExpanded = expandedEventId == event.id
            Row(Modifier.height(IntrinsicSize.Min)) {
                // Timeline Visuals (Left)
                Box(Modifier.width(16.dp).fillMaxHeight()) {
                    // Vertical Line
                    Box(
                        Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(Stone300)
                            .align(Alignment.Center)
                    )
                    // Dot
                    Box(
                        Modifier
                            .size(12.dp)
                            .background(BeigeBackground)
                            .border(2.dp, Color.Red, CircleShape)
                            .align(Alignment.TopCenter)
                    ) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .background(Color.Red, CircleShape)
                                .align(Alignment.Center)
                        )
                    }
                }

                // Event Card (Right)
                Card(
                    modifier = Modifier
                        .padding(start = 8.dp, bottom = 16.dp)
                        .fillMaxWidth()
                        .clickable { expandedEventId = if (isExpanded) null else event.id },
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(
                                    text = event.time,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Stone500
                                )
                                Text(
                                    text = "${event.appSource} • ${event.heartRate} BPM",
                                    fontWeight = FontWeight.Medium,
                                    color = Stone900
                                )
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Stone500
                            )
                        }

                        // Expanded OCR Content
                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFF5F5F4), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFE7E5E4), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = "\"${event.ocrSnippet}\"",
                                        fontSize = 14.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = Stone500
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
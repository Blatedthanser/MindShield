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
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.mindshield.data.repository.InterventionRepository
import com.example.mindshield.ui.theme.*
import f
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import w
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightScreen() {

    var expandedEventId by remember { mutableStateOf<Long?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val realEvents by InterventionRepository.events.collectAsState(initial = emptyList())

    val stressData by remember(realEvents) {
        derivedStateOf { InterventionRepository.getHourlyStressData(realEvents) }
    }

    val appRankingData by remember(realEvents) {
        derivedStateOf { InterventionRepository.getAppRankingData(realEvents) }
    }

    // 刷新逻辑
    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            delay(1000)
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BeigeBackground),
            contentPadding = PaddingValues(24.w)
        ) {
            item {
                Text(
                    text = "Insight",
                    fontSize = 24.f,
                    fontWeight = FontWeight.Bold,
                    color = Stone800,
                    modifier = Modifier.padding(bottom = 24.w)
                )
            }

            // --- 1. Stress Time Distribution Chart ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBeige),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.w)
                        .border(1.w, Stone300, RoundedCornerShape(12.w)),
                    shape = RoundedCornerShape(12.w)
                ) {
                    Column(Modifier.padding(16.w)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(16.w), tint = Color(0xFF4F46E5))
                            Spacer(Modifier.width(8.w))
                            Text("Stress Time Distribution", fontSize = 14.f, color = Stone600)
                        }
                        Spacer(Modifier.height(16.w))

                        if (stressData.all { it.count == 0 }) {
                            Box(Modifier.fillMaxWidth().height(180.w), contentAlignment = Alignment.Center) {
                                Text("No Data Yet", color = Stone500, fontSize = 14.f)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(180.w),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val hourlyMax = remember(stressData) { stressData.maxOfOrNull { it.count } ?: 0 }
                                val safeMax = if (hourlyMax == 0) 1 else hourlyMax

                                stressData.forEach { data ->
                                    // 计算高度
                                    val rawHeightValue = (data.count.toFloat() / safeMax * 120)
                                    val finalBarHeight = if (rawHeightValue < 4f) 4.w else rawHeightValue.toDouble().w

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.wrapContentHeight()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(24.w)
                                                .height(finalBarHeight)
                                                .background(
                                                    if (data.count == hourlyMax && data.count > 0) Orange600 else Color(0xFFA8A29E),
                                                    RoundedCornerShape(4.w, 4.w, 0.w, 0.w)
                                                )
                                        )
                                        Spacer(Modifier.height(4.w))
                                        Text(data.name, fontSize = 10.f, color = Stone600)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.w))
            }

            // --- 2. Most Irritable Apps Ranking ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBeige),
                    modifier = Modifier.fillMaxWidth().border(1.w, Stone300, RoundedCornerShape(12.w)),
                    shape = RoundedCornerShape(12.w)
                ) {
                    Column(modifier = Modifier.padding(16.w)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.w)) {
                            Icon(Icons.Outlined.Warning, null, modifier = Modifier.size(16.w), tint = Orange600)
                            Spacer(modifier = Modifier.width(8.w))
                            Text("Most Irritable Apps", fontSize = 14.f, color = Stone600)
                        }

                        if (appRankingData.isEmpty()) {
                            Text("No ranking data available.", fontSize = 14.f, color = Stone500)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.w)) {
                                appRankingData.forEachIndexed { index, item ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = (index + 1).toString().padStart(2, '0'),
                                            fontSize = 12.f,
                                            fontFamily = FontFamily.Monospace,
                                            color = Stone500,
                                            modifier = Modifier.width(28.w)
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.w),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(item.name, fontSize = 12.f, color = Stone900)
                                                Text("${item.count}", fontSize = 12.f, color = Stone500)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.w)
                                                    .clip(RoundedCornerShape(3.w))
                                                    .background(Stone300)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(item.percentage / 100f)
                                                        .height(6.w)
                                                        .background(
                                                            color = Orange600.copy(alpha = 1f - (index * 0.2f)),
                                                            shape = RoundedCornerShape(3.w)
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.w))
            }

            // --- 3. Emotional Timeline Header ---
            item {
                Text(
                    text = "Emotional Timeline",
                    fontSize = 18.f,
                    fontWeight = FontWeight.SemiBold,
                    color = Stone800,
                    modifier = Modifier.padding(bottom = 16.w)
                )
            }

            // --- 4. Timeline Events ---
            if (realEvents.isEmpty()) {
                item { Text("No timeline events yet.", color = Stone500, fontSize = 18.f) }
            }

            items(realEvents) { event ->
                val isExpanded = expandedEventId == event.id
                val timeStr = remember(event.timestamp) {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(event.timestamp))
                }

                Row(Modifier.height(IntrinsicSize.Min)) {
                    Box(Modifier.width(16.w).fillMaxHeight()) {
                        Box(Modifier.width(2.w).fillMaxHeight().background(Stone300).align(Alignment.Center))
                        Box(Modifier.size(12.w).background(BeigeBackground).border(2.w, Color.Red, CircleShape).align(Alignment.TopCenter)) {
                            Box(Modifier.size(6.w).background(Color.Red, CircleShape).align(Alignment.Center))
                        }
                    }

                    Card(
                        modifier = Modifier
                            .padding(start = 8.w, bottom = 16.w)
                            .fillMaxWidth()
                            .clickable { expandedEventId = if (isExpanded) null else event.id },
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                        border = BorderStroke(1.w, Color.White.copy(alpha = 0.5f))
                    ) {
                        Column(Modifier.padding(16.w)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(
                                        text = timeStr,
                                        fontSize = 12.f,
                                        fontFamily = FontFamily.Monospace,
                                        color = Stone500
                                    )
                                    Text(
                                        text = "${event.appName} • ${event.heartRate} BPM",
                                        fontWeight = FontWeight.Medium,
                                        color = Stone900,
                                        fontSize = 16.f
                                    )
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Stone500
                                )
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 12.w)) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFF5F5F4), RoundedCornerShape(8.w))
                                            .border(1.w, Color(0xFFE7E5E4), RoundedCornerShape(8.w))
                                            .padding(12.w)
                                            .fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "\"${event.ocrSnippet}\"",
                                            fontSize = 14.f,
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
}
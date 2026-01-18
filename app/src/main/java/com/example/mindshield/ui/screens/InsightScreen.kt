package com.example.mindshield.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mindshield.data.repository.APP_RANKING_DATA
import com.example.mindshield.data.repository.HOURLY_STRESS_DATA
import com.example.mindshield.data.repository.MOCK_EVENTS
import com.example.mindshield.ui.theme.BeigeBackground
import com.example.mindshield.ui.theme.CardBeige
import com.example.mindshield.ui.theme.Orange600
import com.example.mindshield.ui.theme.Stone300
import com.example.mindshield.ui.theme.Stone500
import com.example.mindshield.ui.theme.Stone600
import com.example.mindshield.ui.theme.Stone800
import com.example.mindshield.ui.theme.Stone900
import f
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import w

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightScreen() {
    var expandedEventId by remember { mutableStateOf<String?>(null) }
    var stressData by remember { mutableStateOf(HOURLY_STRESS_DATA) }
    var appRankingData by remember { mutableStateOf(APP_RANKING_DATA) }
    var timelineEvents by remember { mutableStateOf(MOCK_EVENTS) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            // 模拟网络请求延迟 1.5秒
            delay(1000)
            stressData = stressData.shuffled()
            appRankingData = appRankingData.shuffled()
            timelineEvents = timelineEvents.shuffled()

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
            // --- Page Title ---
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
                    colors = CardDefaults.cardColors(containerColor = CardBeige),//Color(0xFFEADDCF)
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.w)
                        .border(1.w, Stone300, RoundedCornerShape(12.w)),
                    shape = RoundedCornerShape(12.w)
                ) {
                    Column(Modifier.padding(16.w)) {
                        // Chart Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.w),
                                tint = Color(0xFF4F46E5)
                            )
                            Spacer(Modifier.width(8.w))
                            Text("Stress Time Distribution", fontSize = 14.f, color = Stone600)
                        }
                        Spacer(Modifier.height(16.w))

                        // Bar Chart Rendering
                        Row(
                            modifier = Modifier.fillMaxWidth().height(180.w),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // --- [修改] 使用 stressData 状态，而不是静态常量 ---
                            val hourlyMax = remember(stressData) { // 监听 stressData 变化
                                stressData.maxOfOrNull { it.value } ?: 0
                            }

                            stressData.forEach { data ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.wrapContentHeight()) {
                                    Box(
                                        modifier = Modifier
                                            .width(24.w)
                                            .height((data.value * 15).w)
                                            .background(
                                                if (data.value == hourlyMax) Orange600 else Color(0xFFA8A29E),
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
                Spacer(Modifier.height(24.w))
            }

            // --- 2. Most Irritable Apps Ranking (New Section) ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBeige),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.w, Stone300, RoundedCornerShape(12.w)),
                    shape = RoundedCornerShape(12.w)
                ) {
                    Column(modifier = Modifier.padding(16.w)) {
                        // Section Header
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.w)) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(16.w),
                                tint = Orange600
                            )
                            Spacer(modifier = Modifier.width(8.w))
                            Text(
                                text = "Most Irritable Apps",
                                fontSize = 14.f,
                                color = Stone600
                            )
                        }

                        // Ranking List
                        Column(verticalArrangement = Arrangement.spacedBy(12.w)) {
                            // --- [修改] 使用 appRankingData 状态 ---
                            appRankingData.forEachIndexed { index, item ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Rank Number (e.g., 01, 02)
                                    Text(
                                        text = "0${index + 1}",
                                        fontSize = 12.f,
                                        fontFamily = FontFamily.Monospace,
                                        color = Stone500,
                                        modifier = Modifier.width(28.w)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        // App Name & Percentage
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.w),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(item.name, fontSize = 12.f, color = Stone900)
                                            Text("${item.value}%", fontSize = 12.f, color = Stone500)
                                        }

                                        // Progress Bar Track
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.w)
                                                .clip(RoundedCornerShape(3.w))
                                                .background(Stone300)
                                        ) {
                                            // Progress Bar Fill
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(item.value / 100f)
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
            // --- [修改] 使用 timelineEvents 状态 ---
            items(timelineEvents) { event ->
                val isExpanded = expandedEventId == event.id
                Row(Modifier.height(IntrinsicSize.Min)) {
                    // Timeline Visuals (Left)
                    Box(Modifier.width(16.w).fillMaxHeight()) {
                        // Vertical Line
                        Box(
                            Modifier
                                .width(2.w)
                                .fillMaxHeight()
                                .background(Stone300)
                                .align(Alignment.Center)
                        )
                        // Dot
                        Box(
                            Modifier
                                .size(12.w)
                                .background(BeigeBackground)
                                .border(2.w, Color.Red, CircleShape)
                                .align(Alignment.TopCenter)
                        ) {
                            Box(
                                Modifier
                                    .size(6.w)
                                    .background(Color.Red, CircleShape)
                                    .align(Alignment.Center)
                            )
                        }
                    }

                    // Event Card (Right)
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
                                        text = event.time,
                                        fontSize = 12.f,
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
    } // PullToRefreshBox End
}
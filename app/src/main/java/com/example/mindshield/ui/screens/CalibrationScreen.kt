package com.example.mindshield.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.mindshield.domain.calibration.MetricStat
import com.example.mindshield.domain.calibration.UserBaseline
import com.example.mindshield.ui.theme.BeigeBackground
import com.example.mindshield.ui.theme.Stone500
import com.example.mindshield.ui.theme.Stone600
import com.example.mindshield.ui.theme.Stone900
import f
import w

@Composable
fun CalibrationScreen(
    onBackClick: () -> Unit = {},
    onRetestClick: () -> Unit = {},
    onClearClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = BeigeBackground,
        topBar = {
            // Simple Top Bar to match the context
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.w, vertical = 24.w),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onBackClick() },
                    modifier = Modifier.size(40.w) // 按钮整体大小（触摸区域）
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(24.w), // 图标视觉大小
                        tint = Stone900
                    )
                }
                Text(
                    text = "Calibration",
                    fontSize = 24.f,
                    fontWeight = FontWeight.Bold,
                    color = Stone900
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.w),
            verticalArrangement = Arrangement.spacedBy(24.w)
        ) {
            // 1. BASELINE DATA DISPLAY
            SettingsGroup(title = "Current Baseline Stats") {
                Column(modifier = Modifier.padding(16.w)) {
                    val isCalibrated by UserBaseline
                        .isCalibrated
                        .collectAsState()
                    if (isCalibrated) {
                        // Custom Grid Layout for Stats
                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatItem(modifier = Modifier.weight(1f), label = "HR (bpm)", stat = UserBaseline.hr)
                            StatItem(modifier = Modifier.weight(1f), label = "RMSSD (ms)", stat = UserBaseline.rmssd)
                        }
                        Spacer(modifier = Modifier.height(16.w))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatItem(modifier = Modifier.weight(1f), label = "SDNN (ms)", stat = UserBaseline.sdnn)
                            StatItem(modifier = Modifier.weight(1f), label = "pNN50 (%)", stat = UserBaseline.pnn50)
                        }
                        Spacer(modifier = Modifier.height(16.w))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatItem(modifier = Modifier.weight(1f), label = "LF Power", stat = UserBaseline.lf)
                            StatItem(modifier = Modifier.weight(1f), label = "HF Power", stat = UserBaseline.hf)
                        }
                    } else {
                        // Empty State
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.w),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No calibration data available.",
                                color = Stone500,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 2. ACTIONS
            SettingsGroup(title = "Actions") {
                // Retest Button
                SettingsItem(
                    icon = Icons.Default.Refresh,
                    label = "Recalibrate",
                    onClick = onRetestClick,
                    showDivider = true
                )

                // Clear Data Button (Destructive style like screenshot)
                SettingsItem(
                    icon = Icons.Default.DeleteOutline,
                    label = "Clear Calibration Data",
                    onClick = onClearClick,
                    isDestructive = true,
                    showDivider = false // Last item needs no divider
                )
            }

            // Footer Info
            Text(
                text = "Baseline data exhibits interindividual variation.\nStandard deviation implies variability range.",
                modifier = Modifier.fillMaxWidth(),
                color = Stone500,
                fontSize = 12.f,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

// ================= HELPER COMPONENTS =================

@Composable
fun StatItem(
    modifier: Modifier = Modifier,
    label: String,
    stat: MetricStat
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.f,
            color = Stone500,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.w))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "%.1f".format(stat.mean),
                fontSize = 20.f,
                fontWeight = FontWeight.Bold,
                color = Stone900
            )
            Spacer(modifier = Modifier.width(4.w))
            Text(
                text = "± %.1f".format(stat.stdDev),
                fontSize = 12.f,
                fontWeight = FontWeight.Normal,
                color = Stone600,
                modifier = Modifier.padding(bottom = 2.w) // Align baseline
            )
        }
    }
}

@Preview
@Composable
fun PreviewCalibration() {
    CalibrationScreen()
}
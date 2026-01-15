package com.example.mindshield.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindshield.domain.calibration.MetricStat
import com.example.mindshield.domain.calibration.UserBaseline
import com.example.mindshield.ui.theme.*

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
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onBackClick() },
                    tint = Stone900
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Calibration",
                    fontSize = 20.sp,
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            // 1. BASELINE DATA DISPLAY
            SettingsGroup(title = "Current Baseline Stats") {
                Column(modifier = Modifier.padding(16.dp)) {
                    val isCalibrated by UserBaseline
                        .isCalibrated
                        .collectAsState()
                    if (isCalibrated) {
                        // Custom Grid Layout for Stats
                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatItem(modifier = Modifier.weight(1f), label = "HR (bpm)", stat = UserBaseline.hr)
                            StatItem(modifier = Modifier.weight(1f), label = "RMSSD (ms)", stat = UserBaseline.rmssd)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatItem(modifier = Modifier.weight(1f), label = "SDNN (ms)", stat = UserBaseline.sdnn)
                            StatItem(modifier = Modifier.weight(1f), label = "pNN50 (%)", stat = UserBaseline.pnn50)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatItem(modifier = Modifier.weight(1f), label = "LF Power", stat = UserBaseline.lf)
                            StatItem(modifier = Modifier.weight(1f), label = "HF Power", stat = UserBaseline.hf)
                        }
                    } else {
                        // Empty State
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
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
                text = "Last updated: Just now\nStandard deviation implies variability range.",
                modifier = Modifier.fillMaxWidth(),
                color = Stone500,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

// ================= HELPER COMPONENTS =================

// Custom composable to display a single metric beautifully
@Composable
fun StatItem(
    modifier: Modifier = Modifier,
    label: String,
    stat: MetricStat
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Stone500,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "%.1f".format(stat.mean),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Stone900
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "± %.1f".format(stat.stdDev),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Stone600,
                modifier = Modifier.padding(bottom = 2.dp) // Align baseline
            )
        }
    }
}

@Preview
@Composable
fun PreviewCalibration() {
    CalibrationScreen()
}
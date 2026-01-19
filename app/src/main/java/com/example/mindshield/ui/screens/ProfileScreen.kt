package com.example.mindshield.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mindshield.ui.theme.*
import android.provider.Settings
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.mindshield.service.AccessibilityState
import f
import w

@Composable
fun ProfileScreen(
    onCalibrationClick: () -> Unit
) {
    val context = LocalContext.current

    val isServiceEnabled by remember(context) {
        AccessibilityState.getEnabledFlow(context)
    }.collectAsState(initial = false)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBeige)
            .verticalScroll(rememberScrollState())
            .padding(24.w)
            .padding(bottom = 80.w)
    ) {
        Text(
            text = "Profile",
            fontSize = 24.f,
            fontWeight = FontWeight.Bold,
            color = Stone900
        )

        // --- User Header ---
        Row(
            modifier = Modifier
                .padding(vertical = 32.w)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(64.w)
                    .clip(CircleShape)
                    .background(Emerald800), // Dark green
                contentAlignment = Alignment.Center
            ) {
                Text("MS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.w))
            Column {
                Text("MindShield User", fontWeight = FontWeight.Medium, fontSize = 16.f, color = Stone900)
                Text("Premium Plan", fontSize = 14.f, color = Stone600)
            }
        }

        // --- Data & Privacy Group ---
        SettingsGroup(title = "Data & Privacy") {
            SettingsItem(
                icon = Icons.Outlined.SdStorage,
                label = "Physiological Data Calibration",
                onClick = onCalibrationClick,
                showChevron = true,
                showDivider = true,
            )
            SettingsItem(
                icon = Icons.Outlined.Delete,
                label = "Clear All Historical Data",
                onClick = {},
                showChevron = false,
                isDestructive = true,
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(24.w))

        // --- Permissions Group ---
        SettingsGroup(title = "Permissions") {
            SettingsItem(
                icon = Icons.Outlined.Security,
                label = "Accessibility Service",
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                },
                showChevron = false,
                showDivider = true,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .background(Color(if (isServiceEnabled) 0x33059669 else 0x339E9E9E), RoundedCornerShape(4.w))
                            .padding(horizontal = 6.w, vertical = 2.w)
                    ) {
                        Text(if (isServiceEnabled) "Active" else "Inactive", fontSize = 11.f, color = if (isServiceEnabled) Emerald800 else Stone500, fontWeight = FontWeight.Bold)
                    }
                }
            )

        }

        // --- Footer ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.w),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("MindShield v0.1.0 Preview", fontSize = 12.f, color = Stone500)
            Spacer(modifier = Modifier.height(4.w))
            Text("Privacy Policy • Terms of Service", fontSize = 12.f, color = Stone500)
        }
    }
}

// 辅助组件：设置分组容器
@Composable
fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.w))
            .background(CardBeige)
            .border(1.w, Color(0xFFD6D3D1), RoundedCornerShape(16.w))
    ) {
        // Group Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x4DCA8A04).copy(alpha = 0.1f)) // Slight dark overlay
                .border(0.w, Color.Transparent) // Hack to match border logic
                .padding(horizontal = 16.w, vertical = 12.w)
        ) {
            Text(title.uppercase(), fontSize = 12.f, fontWeight = FontWeight.Bold, color = Stone600, letterSpacing = 1.sp)
        }
        Divider(color = Color(0xFFD6D3D1), thickness = 1.w)

        content()
    }
}

// 辅助组件：设置单项
@Composable
fun SettingsItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    showChevron: Boolean = true,
    isDestructive: Boolean = false,
    showDivider: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val contentColor = if (isDestructive) Color(0xFFDC2626) else Stone900 // Red 600 or Stone 900
    val iconColor = if (isDestructive) Color(0xFFDC2626) else Stone500

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.w),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.w))
        Spacer(modifier = Modifier.width(12.w))
        Text(label, fontSize = 14.f, color = contentColor, modifier = Modifier.weight(1f))

        if (trailingContent != null) {
            trailingContent()
        } else if (showChevron) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Stone500, modifier = Modifier.size(16.w))
        }
    }
    if (showDivider) {
        Divider(color = Color(0xFFD6D3D1), thickness = 1.w)
    }
}

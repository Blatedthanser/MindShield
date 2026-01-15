package com.example.mindshield.ui.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindshield.ui.theme.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterventionScreen() {

    var sensitivity by remember { mutableFloatStateOf(0.5f) }
    var edgeGlow by remember { mutableStateOf(true) }
    var desaturation by remember { mutableStateOf(false) }
    var hapticsEnabled by remember { mutableStateOf(true) }
    var heartbeatSync by remember { mutableStateOf(true) }
    var bubbleAlert by remember { mutableStateOf(true) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val thumbSize by animateDpAsState(
        targetValue = if (isPressed || isDragged) 32.dp else 24.dp,
        label = "ThumbSizeAnimation"
    )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmBeige)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(bottom = 80.dp)
        ) {
            Text(
                text = "Intervention",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Stone900
            )
            Text(
                text = "Changeable Parameters on How Mindshield Can Protect You",
                fontSize = 12.sp,
                color = Stone600,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // --- Trigger Sensitivity Section ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Trigger Sensitivity", fontWeight = FontWeight.Medium, color = Stone900)
                ContainerLabel(text = "${(sensitivity * 100).toInt()}%")
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBeige)
                    .border(1.dp, Color(0xFFD6D3D1), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Slider(
                    value = sensitivity,
                    onValueChange = { sensitivity = it },
                    interactionSource = interactionSource,
                    colors = SliderDefaults.colors(
                        thumbColor = Emerald600,
                        // Set the color of the initial track transparent because we have to redefine it.
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(thumbSize)
                                .shadow(elevation = 4.dp, shape = CircleShape)
                                .background(color = Color.White, shape = CircleShape)
                                .padding(3.dp)
                                .background(color = Emerald600, shape = CircleShape)
                        )
                    },
                    // Personal track setting
                    track = { sliderState ->
                        // 设定轨道的高度和圆角
                        val trackHeight = 14.dp // adjust the thickness of the track
                        val trackShape = RoundedCornerShape(trackHeight / 2)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(trackHeight) // 限制轨道高度
                                .clip(trackShape)    // 裁剪圆角
                                .background(Color(0xFFD6D3D1)) // 1. 先画完整的灰色背景 (贯穿整个长度)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = sliderState.value)
                                    .fillMaxHeight()
                                    .background(Emerald600)
                            )
                        }
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Dull", fontSize = 14.sp, color = Stone500)
                    Text("Sensitive", fontSize = 14.sp, color = Stone500)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // --- Visual Cooling Section ---
            SectionHeader(
                icon = Icons.Outlined.Visibility,
                title = "Visual Cooling",
                color = Color(0xFF2563EB)
            ) // Blue
            CardGroup {
                SwitchRow(
                    title = "Screen Edge Glow",
                    subtitle = "Breathing light rhythm",
                    checked = edgeGlow,
                    onCheckedChange = { edgeGlow = it },
                    showDivider = true
                )
                SwitchRow(
                    title = "Color Desaturation",
                    subtitle = "Slowly reduce screen vibrancy",
                    checked = desaturation,
                    onCheckedChange = { desaturation = it },
                    showDivider = false
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // --- Haptic Feedback Section ---
            SectionHeader(
                icon = Icons.Outlined.Smartphone,
                title = "Haptic Feedback",
                color = Color(0xFF9333EA)
            ) // Purple
            CardGroup {
                SwitchRow(
                    title = "Wrist Vibration",
                    subtitle = "Short, firm pulses to interrupt anger",
                    checked = hapticsEnabled,
                    onCheckedChange = { hapticsEnabled = it },
                    showDivider = hapticsEnabled // 如果开启，下面还有一行，显示分割线
                )
                if (hapticsEnabled) {
                    // Heartbeat Sync Sub-item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBeige) // Yellowish tint
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFCA8A04),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Heartbeat Sync",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Stone900
                                )
                            }
                            Text(
                                "Vibrate 20% slower than current HR",
                                fontSize = 12.sp,
                                color = Stone500
                            )
                        }
                        Switch(
                            checked = heartbeatSync,
                            onCheckedChange = { heartbeatSync = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Emerald600,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFA8A29E)
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // --- Cognitive Section ---
            SectionHeader(
                icon = Icons.Outlined.Message,
                title = "Cognitive",
                color = Color(0xFFDB2777)
            ) // Pink
            CardGroup {
                SwitchRow(
                    title = "Floating Bubble",
                    subtitle = "Gentle \"MindShield Active\" overlay",
                    checked = bubbleAlert,
                    onCheckedChange = { bubbleAlert = it },
                    showDivider = false
                )
            }

            // --- Demo Info Box ---
            if (edgeGlow) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xF0EFF6FF), RoundedCornerShape(12.dp)) // Blue 50
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "The \"Edge Glow\" effect is active. You will see a breathing blue border on your screen when stress is detected.",
                        fontSize = 12.sp,
                        color = Color(0xFF1E40AF),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    // 辅助组件：百分比标签
    @Composable
    fun ContainerLabel(text: String) {
        Box(
            modifier = Modifier
                .background(Color(0x1A059669), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(text, fontSize = 12.sp, color = Emerald800, fontWeight = FontWeight.Bold)
        }
    }

    // 辅助组件：部分标题
    @Composable
    fun SectionHeader(icon: ImageVector, title: String, color: Color) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Medium, color = Stone900)
        }
    }

    // 辅助组件：卡片容器
    @Composable
    fun CardGroup(content: @Composable () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBeige)
                .border(1.dp, Color(0xFFD6D3D1), RoundedCornerShape(16.dp))
        ) {
            content()
        }
    }

    // 辅助组件：开关行
    @Composable
    fun SwitchRow(
        title: String,
        subtitle: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        showDivider: Boolean
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Stone900)
                Text(subtitle, fontSize = 12.sp, color = Stone500)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Emerald600,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFA8A29E) // stone-400
                )
            )
        }
        if (showDivider) {
            Divider(color = Color(0xFFD6D3D1), thickness = 1.dp)
        }
    }
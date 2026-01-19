package com.example.mindshield.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mindshield.ui.theme.CardBeige
import com.example.mindshield.ui.theme.Emerald600
import com.example.mindshield.ui.theme.Emerald800
import com.example.mindshield.ui.theme.Stone500
import com.example.mindshield.ui.theme.Stone600
import com.example.mindshield.ui.theme.Stone900
import com.example.mindshield.ui.theme.WarmBeige
import com.example.mindshield.ui.viewmodel.InterventionScreenViewModel
import f
import w


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterventionScreen(viewModel: InterventionScreenViewModel) {
    val sensitivity by viewModel.triggerSensitivityState.collectAsState()
    val edgeGlow by viewModel.screenEdgeGlow.collectAsState()
    val desaturation by viewModel.colorDesaturation.collectAsState()
    val hapticsEnabled by viewModel.wristVibration.collectAsState()
    val heartbeatSync by viewModel.heartbeatSync.collectAsState()
    val bubbleAlert by viewModel.floatingBubble.collectAsState()

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()

    val thumbSize by animateDpAsState(
        targetValue = if (isPressed || isDragged) 32.w else 24.w,
        label = "ThumbSizeAnimation"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBeige)
            .verticalScroll(rememberScrollState())
            .padding(24.w)
            .padding(bottom = 80.w)
    ) {
        Text(
            text = "Intervention",
            fontSize = 24.f,
            fontWeight = FontWeight.Bold,
            color = Stone900
        )
        Text(
            text = "Changeable Parameters on How Mindshield Can Protect You",
            fontSize = 12.f,
            color = Stone600,
            modifier = Modifier.padding(bottom = 32.w)
        )

        // --- Trigger Sensitivity Section ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.w),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Trigger Sensitivity", fontWeight = FontWeight.Medium, color = Stone900, fontSize = 16.f, lineHeight = 24.f)
            ContainerLabel(text = "${sensitivity}%")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.w))
                .background(CardBeige)
                .border(1.w, Color(0xFFD6D3D1), RoundedCornerShape(16.w))
                .padding(18.w)
        ) {
            Slider(
                value = (sensitivity / 100f),
                onValueChange = { viewModel.updateTriggerSensitivity((it * 100).toInt()) },
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
                            .shadow(elevation = 4.w, shape = CircleShape)
                            .background(color = Color.White, shape = CircleShape)
                            .padding(3.w)
                            .background(color = Emerald600, shape = CircleShape)
                    )
                },
                // Personal track setting
                track = { sliderState ->
                    // 设定轨道的高度和圆角
                    val trackHeight = 14.w // adjust the thickness of the track
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
                Text("Dull", fontSize = 14.f, color = Stone500, lineHeight = 5.f)
                Text("Sensitive", fontSize = 14.f, color = Stone500, lineHeight = 5.f)
            }
        }
        Spacer(modifier = Modifier.height(32.w))

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
                onCheckedChange = { viewModel.setScreenEdgeGlow(it) },
                showDivider = true
            )
            SwitchRow(
                title = "Color Desaturation",
                subtitle = "Slowly reduce screen vibrancy",
                checked = desaturation,
                onCheckedChange = { viewModel.setColorDesaturation(it) },
                showDivider = false
            )
        }
        Spacer(modifier = Modifier.height(32.w))

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
                onCheckedChange = { viewModel.setWristVibration(it) },
                showDivider = hapticsEnabled // 如果开启，下面还有一行，显示分割线
            )
            if (hapticsEnabled == true) {
                // Heartbeat Sync Sub-item
                SwitchRow(
                    title = "Heartbeat Sync",
                    subtitle = "Vibrate 20% slower than current HR",
                    checked = heartbeatSync,
                    onCheckedChange = { viewModel.setHeartbeatSync(it) },
                    showDivider = false // 如果开启，下面还有一行，显示分割线
                )
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(CardBeige) // Yellowish tint
//                        .padding(16.w),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Column(modifier = Modifier.weight(1f)) {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Icon(
//                                imageVector = Icons.Outlined.Bolt,
//                                contentDescription = null,
//                                tint = Color(0xFFCA8A04),
//                                modifier = Modifier.size(14.w)
//                            )
//                            Spacer(modifier = Modifier.width(4.w))
//                            Text(
//                                "Heartbeat Sync",
//                                fontSize = 14.f,
//                                fontWeight = FontWeight.Medium,
//                                color = Stone900
//                            )
//                        }
//                        Text(
//                            "Vibrate 20% slower than current HR",
//                            fontSize = 12.f,
//                            color = Stone500
//                        )
//                    }
//                    if (heartbeatSync != null){
//                        Switch(
//                            checked = heartbeatSync == true,
//                            onCheckedChange = { viewModel.setHeartbeatSync(it) },
//                            colors = SwitchDefaults.colors(
//                                checkedThumbColor = Color.White,
//                                checkedTrackColor = Emerald600,
//                                uncheckedThumbColor = Color.White,
//                                uncheckedTrackColor = Color(0xFFA8A29E)
//                            )
//                        )
//                    }
//                    else {
//                        //
//                    }
//
//                }
            }
        }
        Spacer(modifier = Modifier.height(32.w))

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
                onCheckedChange = { viewModel.setFloatingBubble(it) },
                showDivider = false
            )
        }

        // --- Demo Info Box ---
        if (edgeGlow == true) {
            Spacer(modifier = Modifier.height(16.w))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xF0EFF6FF), RoundedCornerShape(12.w)) // Blue 50
                    .border(1.w, Color(0xFFBFDBFE), RoundedCornerShape(12.w))
                    .padding(16.w),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(20.w)
                )
                Spacer(modifier = Modifier.width(12.w))
                Text(
                    "The \"Edge Glow\" effect is active. You will see a breathing blue border on your screen when stress is detected.",
                    fontSize = 12.f,
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
                .background(Color(0x1A059669), RoundedCornerShape(4.w))
                .padding(horizontal = 8.w, vertical = 4.w)
        ) {
            Text(text, fontSize = 12.f, color = Emerald800, fontWeight = FontWeight.Bold)
        }
    }

    // 辅助组件：部分标题
    @Composable
    fun SectionHeader(icon: ImageVector, title: String, color: Color) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.w)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.w)
            )
            Spacer(modifier = Modifier.width(8.w))
            Text(title, fontWeight = FontWeight.Medium, color = Stone900, fontSize = 16.f)
        }
    }

    // 辅助组件：卡片容器
    @Composable
    fun CardGroup(content: @Composable () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.w))
                .background(CardBeige)
                .border(1.w, Color(0xFFD6D3D1), RoundedCornerShape(16.w))
        ) {
            content()
        }
    }

    // 辅助组件：开关行
    @Composable
    fun SwitchRow(
        title: String,
        subtitle: String,
        checked: Boolean?,
        onCheckedChange: (Boolean) -> Unit,
        showDivider: Boolean?
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.w),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.f, fontWeight = FontWeight.Medium, color = Stone900, lineHeight = 24.f)
                Text(subtitle, fontSize = 12.f, color = Stone500, lineHeight = 22.f)
            }
            if (checked != null) {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Emerald600,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFA8A29E) // stone-400
                    ),
                    modifier = Modifier.scale(0.80f).height(32.w).width(52.w)
                )
            }
            else {
                //
            }

        }
        if (showDivider == true) {
            Divider(color = Color(0xFFD6D3D1), thickness = 1.w)
        }
    }
package com.example.mindshield.ui.intervention

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EmotionalBubbleOverlay(
    durationMillis: Int = 15_000,
    text: String = "检测到您情绪不稳，\n还好吗？",
    onFinished: () -> Unit
) {
    // --- 描边进度动画 (0 -> 1) ---
    val borderProgress = remember { Animatable(0f) }
    val borderAlpha = remember { Animatable(0.5f) }

    // --- 内容透明度 ---
    val textAlpha = remember { Animatable(0f) }
    val backgroundAlpha = remember { Animatable(0f) }


    // --- 雾气扰动动画 (更剧烈、更明显) ---
    val infiniteTransition = rememberInfiniteTransition(label = "fog")

    // 定义3个光斑，做大幅度运动
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing), // 6秒一圈，明显的流动
            repeatMode = RepeatMode.Reverse
        ), label = "time"
    )

    val borderRotation by infiniteTransition.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing), // 6秒转一圈
            repeatMode = RepeatMode.Reverse
        ), label = "rotation"
    )

    LaunchedEffect(Unit) {
        // 进场：描边 + 缓慢浮现
        // 边框画一圈耗时 5秒
        launch {
            borderProgress.animateTo(1f, tween(5000, easing = FastOutSlowInEasing))
        }
        launch {
            borderAlpha.animateTo(1f,tween(3000, easing = FastOutSlowInEasing))
        }
        // 内容延迟 200ms 后开始渐显，耗时 5秒
        launch {
            delay(200)
            textAlpha.animateTo(0.75f, tween(5000, easing = FastOutSlowInEasing))
        }

        launch {
            delay(900)
            backgroundAlpha.animateTo(0.75f, tween(2800, easing = LinearOutSlowInEasing))
        }

        // 停留
        val stayTime = if (durationMillis > 2500) durationMillis else 2000
        delay(stayTime.toLong())

        // 离场：光线倒退消失 + 雾气消散
        launch {
            borderProgress.animateTo(0f, tween(2000, easing = FastOutSlowInEasing))
        }
        launch {
            textAlpha.animateTo(0f, tween(1000, easing = FastOutSlowInEasing))
        }
        launch {
            backgroundAlpha.animateTo(0f, tween(1000, easing = FastOutSlowInEasing))
        }
        launch {
            borderAlpha.animateTo(0f,tween(1000, easing = FastOutSlowInEasing))
        }
        delay(1000)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // 气泡容器
        Box(
            modifier = Modifier
                .padding(top = 75.dp)
                .widthIn(max = 300.dp)
                .height(IntrinsicSize.Min)
        ) {
            // Layer 1: 动态雾气背景
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val radius = 24.dp.toPx() // 圆角

                // 限制绘制区域在圆角矩形内
                clipPath(Path().apply { addRoundRect(RoundRect(0f, 0f, w, h, CornerRadius(radius))) }) {

                    // 绘制半透明白底 (基底)
                    drawRect(Color.White.copy(alpha = 0.2f * backgroundAlpha.value))

                    // 计算扰动坐标 (基于正弦波，大幅度游走)
                    // 三个游走的散光
                    val t = time

                    // 光团1 (青蓝): 左上 <-> 右下
                    val x1 = w * (0.3f + 0.4f * cos(t))
                    val y1 = h * (0.3f + 0.4f * sin(t))

                    // 光团2 (淡紫): 右上 <-> 左下
                    val x2 = w * (0.7f - 0.3f * sin(t * 0.8f))
                    val y2 = h * (0.6f - 0.3f * cos(t * 0.8f))

                    // 光团3 (柔白高光): 不规则游走
                    val x3 = w * (0.5f + 0.3f * cos(t * 1.5f))
                    val y3 = h * (0.5f + 0.2f * sin(t * 1.2f))

                    // 绘制光团 (使用巨大的半径，形成雾气感)
                    val fogAlpha = backgroundAlpha.value  // 随进场动画变化


                    // 蓝
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF4FC3F7).copy(alpha = 0.55f * fogAlpha), Color.Transparent),
                            center = Offset(x1, y1),
                            radius = w * 0.9f // 巨大半径
                        ),
                        center = Offset(x1, y1),
                        radius = w * 0.9f
                    )

                    // 紫
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFE1BEE7).copy(alpha = 0.70f * fogAlpha), Color.Transparent),
                            center = Offset(x2, y2),
                            radius = w * 0.8f
                        ),
                        center = Offset(x2, y2),
                        radius = w * 0.8f
                    )

                    // 白
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.6f * fogAlpha), Color.Transparent),
                            center = Offset(x3, y3),
                            radius = w * 0.6f
                        ),
                        center = Offset(x3, y3),
                        radius = w * 0.6f
                    )
                }
            }

            // Layer 2: 灵动描边
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val radius = 24.dp.toPx()

                // 定义路径：圆角矩形
                val path = Path().apply {
                    addRoundRect(RoundRect(0f, 0f, w, h, CornerRadius(radius)))
                }

                // 测量路径长度
                val pathMeasure = PathMeasure()
                pathMeasure.setPath(path, false)
                val length = pathMeasure.length

                // 计算当前需要画多长
                val drawLength = length * borderProgress.value

                // 绘制发光边框 (带 PathEffect 实现描边动画)
                // 1. 底层：稍微宽一点的模糊光晕
                if (borderProgress.value > 0) {
                    val cx = w / 2
                    val cy = h / 2

                    val colors = intArrayOf(
                        Color(0xFF66CCFF).toArgb(), // 蓝
                        Color(0xFFFFFFFF).toArgb(), // 白
                        Color(0xFFE1BEE7).toArgb(), // 紫
                        Color(0xFF4FC3F7).toArgb()  // 闭环
                    )
                    val shader = android.graphics.SweepGradient(cx, cy, colors, null).apply {
                        val matrix = android.graphics.Matrix()
                        matrix.setRotate(borderRotation, cx, cy) // 让颜色转起来
                        setLocalMatrix(matrix)
                    }

                    val paintGlow = Paint().asFrameworkPaint().apply {
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 6.dp.toPx()
                        this.shader = shader
                        alpha = (100 * borderAlpha.value).toInt()
                        maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
                        pathEffect = android.graphics.DashPathEffect(floatArrayOf(drawLength, length), 0f)
                    }
                    drawContext.canvas.nativeCanvas.drawPath(path.asAndroidPath(), paintGlow)

                    // 2. 核心层：细的、亮的线条
                    val paintLine = Paint().asFrameworkPaint().apply {
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 2.dp.toPx()
                        this.shader = shader
                        strokeCap = android.graphics.Paint.Cap.ROUND
                        alpha = (120 * borderAlpha.value).toInt()
                        pathEffect = android.graphics.DashPathEffect(floatArrayOf(drawLength, length), 0f)
                    }
                    drawContext.canvas.nativeCanvas.drawPath(path.asAndroidPath(), paintLine)
                }
            }

            // Layer 3: 文字内容
            Text(
                text = text,
                color = Color(0xFF37474F).copy(alpha = textAlpha.value),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .align(Alignment.Center)
            )
        }
    }
}
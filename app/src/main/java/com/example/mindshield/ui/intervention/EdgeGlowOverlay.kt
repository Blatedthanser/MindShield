package com.example.mindshield.ui.intervention

import android.graphics.BlurMaskFilter
import android.graphics.Matrix
import android.graphics.SweepGradient
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 【配色方案】：浓郁、纯净的蓝白调

val DeepBlue = Color(0xFF00a3ff)   // 深邃蓝
val SkyBlue = Color(0xFF00fff0)    // 天空蓝
val ElectricCyan = Color(0xFF66CCFF)


@Composable
fun EdgeGlowOverlay(
    repeatCount: Int,
    rotationDuration: Int, // 颜色流动周期(ms)
    breathDuration: Int,  // 明暗变化周期(ms)
    onAnimationFinished: () -> Unit
) {
    // 呼吸动画 (透明度)
    val breathAnim = remember { Animatable(0f) }

    // 流动动画 (旋转)
    val infiniteTransition = rememberInfiniteTransition(label = "flow")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(rotationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    LaunchedEffect(repeatCount) {
        val halfBreath = breathDuration / 2

        repeat(repeatCount) {
            // 吸气：变亮
            breathAnim.animateTo(1f, tween(halfBreath, easing = EaseInOutSine))
            // 呼气：变暗
            breathAnim.animateTo(0f, tween(halfBreath, easing = EaseInOutSine))
        }
        onAnimationFinished()
    }

    val strokeWidth = 12.dp  // 核心发光体的宽度
    val blurRadius = 20.dp   // 光晕扩散的范围
    val cornerRadius = 0.dp  // 直角适配模拟器

    Box(modifier = Modifier.fillMaxSize()) {
        FlowingGlowBorder(
            rotation = rotation,
            alpha = breathAnim.value,
            strokeWidth = strokeWidth,
            blurRadius = blurRadius,
            cornerRadius = cornerRadius
        )
    }
}

@Composable
fun FlowingGlowBorder(
    rotation: Float,
    alpha: Float,
    strokeWidth: Dp,
    blurRadius: Dp,
    cornerRadius: Dp
) {
    val paint = remember { Paint() }
    val density = LocalDensity.current

    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val blurRadiusPx = with(density) { blurRadius.toPx() }
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }

    val colors = remember {
        intArrayOf(
            DeepBlue.toArgb(),
            SkyBlue.toArgb(),
            ElectricCyan.toArgb(),
            DeepBlue.toArgb()
        )
    }

    val matrix = remember { Matrix() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2

        drawIntoCanvas { canvas ->
            paint.style = PaintingStyle.Stroke
            paint.strokeWidth = strokeWidthPx

            paint.asFrameworkPaint().alpha = (alpha * 255).toInt()

            val shader = SweepGradient(cx, cy, colors, null)

            matrix.setRotate(rotation, cx, cy)
            shader.setLocalMatrix(matrix)

            paint.asFrameworkPaint().shader = shader

            if (blurRadiusPx > 0) {
                paint.asFrameworkPaint().maskFilter =
                    BlurMaskFilter(blurRadiusPx, BlurMaskFilter.Blur.NORMAL)
            }

            canvas.drawRoundRect(
                left = 0f,
                top = 0f,
                right = w,
                bottom = h,
                radiusX = cornerRadiusPx,
                radiusY = cornerRadiusPx,
                paint = paint
            )
        }
    }
}
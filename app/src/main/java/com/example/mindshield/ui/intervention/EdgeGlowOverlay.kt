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

// 【新配色方案】：更浓郁、更纯净的蓝白调
// 使用 Material Design 的高饱和度色值 (A400/A700 系列)
val DeepBlue = Color(0xFF00a3ff)   // 深邃蓝
val SkyBlue = Color(0xFF00fff0)    // 天空蓝
val ElectricCyan = Color(0xFF66CCFF)
val PureWhite = Color(0xFFFFFFFF)  // 纯白高光

@Composable
fun CalmGlowOverlay(
    repeatCount: Int,
    rotationDuration: Int, // 控制流速 (毫秒)，即颜色流动一圈耗时
    breathDuration: Int,  // 单次呼吸总时间，即明暗变化耗时
    onAnimationFinished: () -> Unit
) {
    // 1. 呼吸动画 (透明度)
    val breathAnim = remember { Animatable(0f) }

    // 2. 流动动画 (旋转)
    val infiniteTransition = rememberInfiniteTransition(label = "flow")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            // 使用传入的 rotationDuration 参数
            animation = tween(rotationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    LaunchedEffect(repeatCount) {
        // 计算半个周期的时间（吸气或呼气各占一半）
        val halfBreath = breathDuration / 2

        repeat(repeatCount) {
            // 吸气：变亮
            breathAnim.animateTo(1f, tween(halfBreath, easing = EaseInOutSine))
            // 呼气：变暗
            breathAnim.animateTo(0f, tween(halfBreath, easing = EaseInOutSine))
        }
        onAnimationFinished()
    }

    // 【关键调整】让光变得更窄、更实
    // strokeWidth: 核心发光体的宽度
    // blurRadius: 光晕扩散的范围。之前是 50dp，现在由你控制
    val strokeWidth = 12.dp  // 变细 (之前18)
    val blurRadius = 20.dp   // 变窄 (之前50)，这样光就不会“糊”进屏幕中间了
    val cornerRadius = 0.dp  // 保持直角适配模拟器

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

    // 【新配色逻辑】
    // 顺序：深蓝 -> 天蓝 -> 白 -> 青 -> 深蓝 (闭环)
    // 这样的排列会让白色像一道流星一样穿过蓝色背景
    val colors = remember {
        intArrayOf(
            DeepBlue.toArgb(),
            SkyBlue.toArgb(),
            //PureWhite.toArgb(), // 白色最亮
            ElectricCyan.toArgb(),
            DeepBlue.toArgb()   // 必须和第一个颜色一样，保证首尾相接无缝隙
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

            // 增加一点基础不透明度，让颜色更浓
            // 之前的 alpha 是 0~1，现在我们让它最亮时完全不透明
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
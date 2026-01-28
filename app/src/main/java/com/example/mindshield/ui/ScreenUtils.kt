import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 设计稿宽度
const val DESIGN_WIDTH = 412f

// 当前的“缩放比例”
val LocalAppDimens = compositionLocalOf { 1f }

@Composable
fun ScreenAdapter(
    // 传入当前的实际窗口宽度，通常配合 BoxWithConstraints 使用
    actualWidth: Dp,
    content: @Composable () -> Unit
) {
    // 计算缩放比例：实际宽度 / 设计稿宽度
    val scale = actualWidth.value / DESIGN_WIDTH

    CompositionLocalProvider(LocalAppDimens provides scale) {
        content()
    }
}

val Int.w: Dp
    @Composable
    get() {
        val scale = LocalAppDimens.current
        return (this * scale).dp
    }

val Double.w: Dp
    @Composable
    get() {
        val scale = LocalAppDimens.current
        return (this * scale).dp
    }

val Int.f: TextUnit
    @Composable
    get() {
        val scale = LocalAppDimens.current
        return (this * scale).sp
    }

val Double.f: TextUnit
    @Composable
    get() {
        val scale = LocalAppDimens.current
        return (this * scale).sp
    }
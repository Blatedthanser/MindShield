import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. 定义你的设计稿宽度 (412f)
const val DESIGN_WIDTH = 412f

// 2. 创建一个 CompositionLocal 来存储当前的“缩放比例”
val LocalAppDimens = compositionLocalOf { 1f }

// 3. 这是一个包裹器，你需要把它包在你 App 的最外层（比如 MainActivity 的 setContent 里）
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

// 4. 新的 .w 实现（现在使用 LocalAppDimens，不再依赖 Configuration）
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

// 5. 新的 .f 实现
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
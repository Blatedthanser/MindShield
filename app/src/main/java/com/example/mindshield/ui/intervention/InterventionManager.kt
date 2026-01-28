package com.example.mindshield.ui.intervention

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.mindshield.service.AlertWindowState
import com.example.mindshield.service.InterventionService

/**
 * 干预管理器
 * 负责对外提供简单的接口，内部处理 Service 的启动和权限判断
 */
object InterventionManager {

    /**
     * 触发模式 A：边缘流光
     */
    fun triggerGlow(
        context: Context,
        repeatCount: Int = 3,
        speed: Int = 4000,
        breath: Int = 4000
    ) {
        startIntervention(context) {
            putExtra(InterventionService.EXTRA_TYPE, InterventionService.TYPE_GLOW)
            putExtra("REPEAT", repeatCount)
            putExtra("SPEED", speed)
            putExtra("BREATH", breath)
        }
    }

    /**
     * 触发模式 B：屏幕褪色
     */
    fun triggerDesaturation(context: Context, durationMillis: Int = 5000) {
        startIntervention(context) {
            putExtra(InterventionService.EXTRA_TYPE, InterventionService.TYPE_DESATURATION)
            putExtra("DURATION", durationMillis)
        }
    }

    fun triggerBubble(
        context: Context,
        text: String = "检测到您情绪不稳，还好吗？",
        durationMillis: Int = 6000
    ) {
        startIntervention(context) {
            putExtra(InterventionService.EXTRA_TYPE, InterventionService.TYPE_BUBBLE)
            putExtra("TEXT", text)
            putExtra("DURATION", durationMillis)
        }
    }

    // 私有统一启动方法，减少重复代码
    private fun startIntervention(context: Context, intentBlock: Intent.() -> Unit) {
        if (!canDrawOverlays(context)) return // 可以优化成无权限提示

        val intent = Intent(context, InterventionService::class.java).apply(intentBlock)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
}


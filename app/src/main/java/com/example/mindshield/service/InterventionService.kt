package com.example.mindshield.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.mindshield.R
import com.example.mindshield.ui.intervention.EdgeGlowOverlay
import com.example.mindshield.ui.intervention.DesaturationOverlay
import com.example.mindshield.ui.intervention.EmotionalBubbleOverlay
import com.example.mindshield.ui.intervention.MyLifecycleOwner

class InterventionService : Service() {

    // 定义常量，用于区分模式
    companion object {
        const val EXTRA_TYPE = "intervention_type"
        const val TYPE_GLOW = "type_glow"
        const val TYPE_DESATURATION = "type_desaturation"
        const val TYPE_BUBBLE = "type_bubble"
    }

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private lateinit var lifecycleOwner: MyLifecycleOwner

    override fun onBind(intent: Intent?): IBinder? = null

    // 核心逻辑：收到命令时，切换 UI
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val type = intent?.getStringExtra(EXTRA_TYPE) ?: TYPE_GLOW

        composeView?.setContent {
            when (type) {
                TYPE_GLOW -> {
                    val repeat = intent?.getIntExtra("REPEAT", 3) ?: 3
                    val speed = intent?.getIntExtra("SPEED", 4000) ?: 4000
                    val breath = intent?.getIntExtra("BREATH", 4000) ?: 4000
                    EdgeGlowOverlay(repeat, speed, breath) { stopSelf() }
                }
                TYPE_DESATURATION -> {
                    val duration = intent?.getIntExtra("DURATION", 5000) ?: 5000
                    DesaturationOverlay(duration) { stopSelf() }
                }
                TYPE_BUBBLE -> {
                    val duration = intent?.getIntExtra("DURATION", 6000) ?: 6000
                    val text = intent?.getStringExtra("TEXT") ?: "检测到您情绪不稳，还好吗？"
                    EmotionalBubbleOverlay(
                        durationMillis = duration,
                        text = text,
                        onFinished = { stopSelf() }
                    )
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        startAsForeground() // 开启前台保活

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        lifecycleOwner = MyLifecycleOwner()
        lifecycleOwner.onCreate()
        lifecycleOwner.onStart()
        lifecycleOwner.onResume()

        setupUniversalWindow()
    }

    // 窗口配置，通用于所有干预 UI
    private fun setupUniversalWindow() {
        val metrics = android.util.DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)

        val params = WindowManager.LayoutParams().apply {
            width = metrics.widthPixels
            height = metrics.heightPixels

            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

            // 关键：全屏、透传点击、硬件加速
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP
        }

        composeView = ComposeView(this).apply {
            this.setViewTreeLifecycleOwner(lifecycleOwner)
            this.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            val viewModelStore = ViewModelStore()
            this.setViewTreeViewModelStoreOwner(object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = viewModelStore
            })
        }

        windowManager.addView(composeView, params)
    }

    // 前台服务通知配置
    private fun startAsForeground() {
        val channelId = "mindshield_intervention"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "MindShield Intervention", NotificationManager.IMPORTANCE_MIN)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("MindShield")
                .setContentText("干预进行中...")
                .setSmallIcon(R.drawable.outline_ecg_heart_24)
                .build()
        } else {
            Notification.Builder(this).build()
        }
        startForeground(999, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleOwner.onDestroy()
        if (composeView != null) {
            windowManager.removeView(composeView)
            composeView = null
        }
    }
}
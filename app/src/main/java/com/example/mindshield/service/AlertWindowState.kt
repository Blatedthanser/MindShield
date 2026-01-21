package com.example.mindshield.service

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

object AlertWindowState {

    fun getEnabledFlow(context: Context): Flow<Boolean> = callbackFlow {
        val appContext = context.applicationContext
        val appOpsManager = appContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val packageName = appContext.packageName

        // 定义检查函数 (API 23+)
        fun check(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(appContext)
            } else {
                true // 低于 6.0 默认有权限，或者通过旧方式判断
            }
        }

        // 1. 发送初始状态
        trySend(check())

        // 只有 API 23 (Android 6.0) 以上才有动态悬浮窗权限概念
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // 2. 定义监听器
            val listener = object : AppOpsManager.OnOpChangedListener {
                override fun onOpChanged(op: String?, packageName: String?) {
                    // 监听 SYSTEM_ALERT_WINDOW 的变化
                    if (op == AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW) {
                        trySend(check())
                    }
                }
            }

            // 3. 注册监听
            // 监听针对我们包名的悬浮窗操作变化
            appOpsManager.startWatchingMode(
                AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
                packageName,
                listener
            )

            // 4. 销毁时注销
            awaitClose {
                appOpsManager.stopWatchingMode(listener)
            }
        } else {
            // Android 6.0 以下不需要监听，直接关闭流
            close()
        }
    }.distinctUntilChanged()
}
package com.example.mindshield.service

import android.content.ComponentName
import android.content.Context
import android.database.ContentObserver
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import android.os.Handler

object AccessibilityState {
    fun getEnabledFlow(context: Context): Flow<Boolean> = callbackFlow {
        val appContext = context.applicationContext
        val contentResolver = appContext.contentResolver

        // 定义检查函数
        fun check(): Boolean = isEnabled(appContext)

        // 1. 发送初始状态
        trySend(check())

        // 2. 注册观察者
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(check())
            }
        }

        contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES),
            false,
            observer
        )

        // 3. 销毁时注销
        awaitClose {
            contentResolver.unregisterContentObserver(observer)
        }
    }.distinctUntilChanged()

    /**
     * 具体的检查逻辑
     */
    private fun isEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, MindShieldAccessibilityService::class.java)

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)

        while (splitter.hasNext()) {
            val componentNameString = splitter.next()
            val component = ComponentName.unflattenFromString(componentNameString)
            if (component == expectedComponentName) return true
        }
        return false
    }
}
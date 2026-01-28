package com.example.mindshield.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_settings")

class UserSettings(private val context: Context) {

    // Keys
    private object Keys {
        val TRIGGER_SENSITIVITY = intPreferencesKey("trigger_sensitivity") // 0 - 100
        val SCREEN_EDGE_GLOW = booleanPreferencesKey("screen_edge_glow")
        val COLOR_DESATURATION = booleanPreferencesKey("color_desaturation")
        val WRIST_VIBRATION = booleanPreferencesKey("wrist_vibration")
        val HEARTBEAT_SYNC = booleanPreferencesKey("heartbeat_sync")
        val FLOATING_BUBBLE = booleanPreferencesKey("floating_bubble")
    }

    // === Flows ===
    val triggerSensitivity: Flow<Int> = context.dataStore.data
        .map { it[Keys.TRIGGER_SENSITIVITY] ?: 50 }

    val screenEdgeGlow: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.SCREEN_EDGE_GLOW] ?: true }

    val colorDesaturation: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.COLOR_DESATURATION] ?: true }

    val wristVibration: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.WRIST_VIBRATION] ?: true }

    val heartbeatSync: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.HEARTBEAT_SYNC] ?: true }

    val floatingBubble: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.FLOATING_BUBBLE] ?: true }

    // === Setters ===
    suspend fun setTriggerSensitivity(value: Int) {
        Log.d("MindShield_Debug", "正在尝试写入数据: $value")
        context.dataStore.edit { it[Keys.TRIGGER_SENSITIVITY] = value.coerceIn(0, 100) }
        Log.d("MindShield_Debug", "数据写入指令已发送")

    }

    suspend fun setScreenEdgeGlow(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SCREEN_EDGE_GLOW] = enabled }
    }

    suspend fun setColorDesaturation(enabled: Boolean) {
        context.dataStore.edit { it[Keys.COLOR_DESATURATION] = enabled }
    }

    suspend fun setWristVibration(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WRIST_VIBRATION] = enabled }
    }

    suspend fun setHeartbeatSync(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HEARTBEAT_SYNC] = enabled }
    }

    suspend fun setFloatingBubble(enabled: Boolean) {
        context.dataStore.edit { it[Keys.FLOATING_BUBBLE] = enabled }
    }

}

package com.example.mindshield.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mindshield.data.preferences.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InterventionScreenViewModelFactory(
    private val userSettings: UserSettings
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InterventionScreenViewModel::class.java)) {
            return InterventionScreenViewModel(userSettings) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
class InterventionScreenViewModel(private val userSettings: UserSettings) : ViewModel() {

    // === Flows ===
    val triggerSensitivity: StateFlow<Int> = userSettings.triggerSensitivity
        .stateIn(viewModelScope, SharingStarted.Lazily, 50)

    val screenEdgeGlow: StateFlow<Boolean> = userSettings.screenEdgeGlow
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val colorDesaturation: StateFlow<Boolean> = userSettings.colorDesaturation
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val wristVibration: StateFlow<Boolean> = userSettings.wristVibration
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val heartbeatSync: StateFlow<Boolean> = userSettings.heartbeatSync
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val floatingBubble: StateFlow<Boolean> = userSettings.floatingBubble
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    // === Update functions ===

    fun setScreenEdgeGlow(enabled: Boolean) {
        viewModelScope.launch {
            userSettings.setScreenEdgeGlow(enabled)
        }
    }

    fun setColorDesaturation(enabled: Boolean) {
        viewModelScope.launch {
            userSettings.setColorDesaturation(enabled)
        }
    }

    fun setWristVibration(enabled: Boolean) {
        viewModelScope.launch {
            userSettings.setWristVibration(enabled)
        }
    }

    fun setHeartbeatSync(enabled: Boolean) {
        viewModelScope.launch {
            userSettings.setHeartbeatSync(enabled)
        }
    }

    fun setFloatingBubble(enabled: Boolean) {
        viewModelScope.launch {
            userSettings.setFloatingBubble(enabled)
        }
    }

    // === Update function for triggerSensitivity with debounce ===
    private val _triggerSensitivityState = MutableStateFlow(triggerSensitivity.value)
    val triggerSensitivityState: StateFlow<Int> = _triggerSensitivityState

    fun updateTriggerSensitivity(value: Int) {
        _triggerSensitivityState.value = value.coerceIn(0, 100)
    }

    init {
        viewModelScope.launch {
            triggerSensitivity.collect { savedValue ->
                // updateTriggerSensitivity 会触发下面的防抖写入，
                // 所以为了避免死循环，这里加个判断
                if (_triggerSensitivityState.value != savedValue) {
                    _triggerSensitivityState.value = savedValue
                }
            }
        }
        viewModelScope.launch {
            _triggerSensitivityState
                .debounce(500) // 改成 500ms
                .collectLatest { uiValue ->
                    // 只有当 UI 的值和 DataStore 里的值不一样时才写，避免死循环
                    if (triggerSensitivity.value != uiValue) {
                        userSettings.setTriggerSensitivity(uiValue)
                    }
                }
        }
    }
}
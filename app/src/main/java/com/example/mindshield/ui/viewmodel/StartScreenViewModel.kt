package com.example.mindshield.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mindshield.data.source.IWearableSource
import com.example.mindshield.data.source.WearableSimulator
import com.example.mindshield.domain.calibration.UserStatisticsTester
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch



class StartScreenViewModel() : ViewModel() {

    sealed interface UiState {
        data object Idle : UiState
        data object Calibrating : UiState
        data object Success : UiState
        data object Error : UiState
    }

    // Private mutable state (internal use)
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    private val _countdown = MutableStateFlow<Int?>(null)


    // 2. PUBLIC immutable state (Exposed to UI)
    val uiState = _uiState.asStateFlow()
    val countdown: StateFlow<Int?> = _countdown
    val showFloatingBox: StateFlow<Boolean> =
        uiState
            .map { it is UiState.Calibrating }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )


    fun startCalibration() {
        viewModelScope.launch {
            // Set Loading
            _uiState.value = UiState.Calibrating

            val durationTime = 15   //45s
            _countdown.value = durationTime

            launch {
                while (isActive && _countdown.value!! >= 0) {
                    delay(1_000)
                    _countdown.value = _countdown.value?.minus(1)
                }
            }

            val baseline = UserStatisticsTester.measureUserBaseline(WearableSimulator, durationTime)

            // 3. Update state based on result
            baseline?.let {
                if (it.isCalibrated) {
                    _uiState.value = UiState.Success
                } else {
                    _uiState.value = UiState.Error
                }
            }
        }
    }
}

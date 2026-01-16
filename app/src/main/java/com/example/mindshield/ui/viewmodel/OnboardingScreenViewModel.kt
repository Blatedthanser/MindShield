package com.example.mindshield.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindshield.data.repository.OnboardingManager
import com.example.mindshield.data.source.WearableSimulator
import com.example.mindshield.domain.calibration.BaselineStorage
import com.example.mindshield.domain.calibration.UserBaseline
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



class OnboardingScreenViewModel(application: Application) : AndroidViewModel(application) {

    sealed interface UiState {
        data object Idle : UiState
        data object Calibrating : UiState
        data object Success : UiState
        data object Error : UiState
    }
    // Private mutable state (internal use)
    private val baselineStorage =
        BaselineStorage(application)

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

    fun loadBaseline() {
        baselineStorage.loadBaseline()
    }
    fun saveBaseline() {
        baselineStorage.saveBaseline()
    }
    fun clearBaseline() {
        baselineStorage.clearBaseline()
    }
    fun startCalibration() {
        viewModelScope.launch {
            // Set Loading
            _uiState.value = UiState.Calibrating

            val durationTime = 30
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
                if (it.isCalibrated.value) {
                    _uiState.value = UiState.Success
                } else {
                    _uiState.value = UiState.Error
                }
            }
            saveBaseline()
            /*println("=== Formatted UserBaseline Data ===")

            val metric= listOf(
                "HR" to UserBaseline.hr,
                "RMSSD" to UserBaseline.rmssd,
                "SDNN" to UserBaseline.sdnn,
                "pNN50" to UserBaseline.pnn50,
                "LF" to UserBaseline.lf,
                "HF" to UserBaseline.hf
            )

            metrics.forEach { (name, stat) ->
                println("$name: ${stat.mean} ± ${stat.stdDev}")
            }
            println("Calibrated: ${UserBaseline.isCalibrated}")*/
        }
    }
}

package com.example.mindshield.data.repository

import com.example.mindshield.domain.analysis.MentalState
import com.example.mindshield.model.HrvMetrics
import com.example.mindshield.model.WearableData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DynamicDataToShieldPage {
    // 1. Holds the current data (Starts with 0, 0)
    private val _currentHr = MutableStateFlow(0)

    private val _currentState = MutableStateFlow(MentalState.NULL)

    // 2. Exposes read-only access for the UI
    val currentHr: StateFlow<Int> = _currentHr.asStateFlow()

    val currentState : StateFlow<MentalState> = _currentState

    // 3. Function for the Service to update the data
    fun updateData(newHr: Int, newState: MentalState) {
        _currentHr.value = newHr
        _currentState.value = newState
    }
}
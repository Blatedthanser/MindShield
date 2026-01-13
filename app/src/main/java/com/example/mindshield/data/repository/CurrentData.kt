package com.example.mindshield.data.repository

import com.example.mindshield.model.WearableData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CurrentData {
    // 1. Holds the current data (Starts with 0, 0)
    private val _currentData = MutableStateFlow(WearableData(0, 0))

    // 2. Exposes read-only access for the UI
    val currentData: StateFlow<WearableData> = _currentData.asStateFlow()

    // 3. Function for the Service to update the data
    fun updateData(newData: WearableData) {
        _currentData.value = newData
    }
}
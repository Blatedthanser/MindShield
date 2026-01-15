package com.example.mindshield.data.source

import com.example.mindshield.model.WearableData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

interface IWearableSource {
    // A stream of data that emits a new value every second
    fun observeData(): SharedFlow<WearableData>
    fun connect()
    fun disconnect()
}
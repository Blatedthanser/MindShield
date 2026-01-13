package com.example.mindshield.data.source

import com.example.mindshield.model.WearableData
import kotlinx.coroutines.flow.Flow

interface IWearableSource {
    // A stream of data that emits a new value every second
    fun observeData(): Flow<WearableData>
    fun connect()
    fun disconnect()
}
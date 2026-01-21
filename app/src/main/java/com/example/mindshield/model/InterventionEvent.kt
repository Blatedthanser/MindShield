package com.example.mindshield.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intervention_events")
data class InterventionEvent(
    @PrimaryKey
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long,
    val appName: String,
    val heartRate: Int,
    val ocrSnippet: String,     //识别到的文字片段
    val result: String
)
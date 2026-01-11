package com.example.mindshield.model


enum class StressLevel { CALM, MODERATE, HIGH }
enum class AppTab { SHIELD, INSIGHT, INTERVENTION, PROFILE }

data class EmotionalEvent(
    val id: String,
    val time: String,
    val appSource: String,
    val keywords: List<String>,
    val heartRate: Int,
    val ocrSnippet: String
)

data class ChartDataPoint(val name: String, val value: Int)
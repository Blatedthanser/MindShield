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

data class WearableData(
    val hr: Int,
    val hrv: HrvMetrics,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChartDataPoint(val name: String, val value: Int)

data class HrvMetrics(
    val rmssd: Double,  // ms: Vagus nerve tone (Most Important)
    val sdnn: Double,   // ms: Overall variability
    val pnn50: Double,  // %:  Another view of parasympathetic tone
    val lf: Double,     // Power: Sympathetic (Fight/Flight)
    val hf: Double,     // Power: Parasympathetic (Rest/Digest)
)
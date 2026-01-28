package com.example.mindshield.model

enum class AppTab { SHIELD, INSIGHT, INTERVENTION, PROFILE }

data class WearableData(
    val hr: Int,
    val hrv: HrvMetrics,
    val timestamp: Long = System.currentTimeMillis()
)

data class HrvMetrics(
    val rmssd: Double,  // ms: Vagus nerve tone (Most Important)
    val sdnn: Double,   // ms: Overall variability
    val pnn50: Double,  // %:  Another view of parasympathetic tone
    val lf: Double,     // Power: Sympathetic (Fight/Flight)
    val hf: Double,     // Power: Parasympathetic (Rest/Digest)
)
package com.example.mindshield.domain.calibration

import com.example.mindshield.data.source.IWearableSource
import com.example.mindshield.domain.analysis.MetricStat
import com.example.mindshield.domain.analysis.UserBaseline
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.pow
import kotlin.math.sqrt

object UserStatisticsTester {

    suspend fun measureUserBaseline(
        source: IWearableSource,
        durationSeconds: Int
    ): UserBaseline? {
        println("CALIBRATION: collecting statistical variance...")

        val samples = withTimeoutOrNull(durationSeconds * 1000L + 2000) {
            source.observeData().take(durationSeconds).toList()
        }

        if (samples.isNullOrEmpty() || samples.size < 5) return null

        // Helper function to calculate Stat
        fun calcStat(values: List<Double>): MetricStat {
            val mean = values.average()
            val variance = values.map { (it - mean).pow(2) }.average()
            val stdDev = sqrt(variance).coerceAtLeast(1.0) // Prevent div by zero
            return MetricStat(mean, stdDev)
        }

        return UserBaseline.apply {
            hr = calcStat(samples.map { it.hr.toDouble() })
            rmssd = calcStat(samples.map { it.hrv.rmssd })
            sdnn = calcStat(samples.map { it.hrv.sdnn })
            pnn50 = calcStat(samples.map { it.hrv.pnn50 })
            lf = calcStat(samples.map { it.hrv.lf })
            hf = calcStat(samples.map { it.hrv.hf })
            isCalibrated = true
        }
    }
}

package com.example.mindshield.domain.calibration

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class MetricStat(
    val mean: Double,
    val stdDev: Double
)

object UserBaseline {
    // We store Mean AND Standard Deviation for every metric
    var hr = MetricStat(65.0, 5.0)
    var rmssd = MetricStat(45.0, 10.0)
    var sdnn = MetricStat(55.0, 15.0)
    var pnn50 = MetricStat(15.0, 5.0)
    var lf = MetricStat(300.0, 100.0)
    var hf = MetricStat(250.0, 80.0)
    private val _isCalibrated = MutableStateFlow(false)
    val isCalibrated: StateFlow<Boolean> = _isCalibrated

    fun setCalibrated(value: Boolean) {
        _isCalibrated.value = value
    }

    fun reset() {
        hr = MetricStat(65.0, 5.0)
        rmssd = MetricStat(45.0, 10.0)
        sdnn = MetricStat(55.0, 15.0)
        pnn50 = MetricStat(15.0, 5.0)
        lf = MetricStat(300.0, 100.0)
        hf = MetricStat(250.0, 80.0)
        _isCalibrated.value = false
    }
}

data class BaselineSnapshot(
    val hr: MetricStat,
    val rmssd: MetricStat,
    val sdnn: MetricStat,
    val pnn50: MetricStat,
    val lf: MetricStat,
    val hf: MetricStat,
    val isCalibrated: Boolean
)
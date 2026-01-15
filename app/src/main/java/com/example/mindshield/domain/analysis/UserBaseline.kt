package com.example.mindshield.domain.analysis

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
    var isCalibrated = false
}
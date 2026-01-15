package com.example.mindshield.domain.analysis

import com.example.mindshield.domain.calibration.UserBaseline
import com.example.mindshield.model.HrvMetrics


enum class MentalState {
    NULL,
    CALM_OR_HAPPY,  // Low HR, High/Normal HRV
    EXCITEMENT,     // High HR, Moderate HRV (Positive Stress)
    DISTRESS        // High HR, Low HRV (Anger/Fear/Anxiety)
}

object PhysiologicalAnalyzer {

    /**
     * Uses Multivariate Statistical Analysis (Z-Scores) to detect anomalies.
     */
    fun analyze(
        currentHr: Int,
        hrv: HrvMetrics,
        baseline: UserBaseline
    ): MentalState {

        if (!baseline.isCalibrated) return MentalState.NULL

        // 1. CALCULATE Z-SCORES (Standardized Distance from Mean)
        // A Z-score of +2.0 means "Higher than 95% of normal readings"
        // A Z-score of -2.0 means "Lower than 95% of normal readings"

        val zHr = (currentHr - baseline.hr.mean) / baseline.hr.stdDev
        val zRmssd = (hrv.rmssd - baseline.rmssd.mean) / baseline.rmssd.stdDev
        val zSdnn = (hrv.sdnn - baseline.sdnn.mean) / baseline.sdnn.stdDev
        val zPnn50 = (hrv.pnn50 - baseline.pnn50.mean) / baseline.pnn50.stdDev

        // LF/HF Ratio Z-Score (Derived)
        val currentRatio = if(hrv.hf > 0) hrv.lf / hrv.hf else 2.0
        val baseRatioMean = baseline.lf.mean / baseline.hf.mean
        // We estimate ratio StdDev roughly as sum of component variances relative (simplified)
        val baseRatioStd = (baseline.lf.stdDev / baseline.hf.mean)
        val zRatio = (currentRatio - baseRatioMean) / baseRatioStd

        // 2. CHECK FOR QUIESCENCE (Is user just sitting?)
        // If HR is within 1 Sigma of mean, you are calm. No complex math needed.
        if (zHr < 1.0) return MentalState.CALM_OR_HAPPY

        // 3. CALCULATE THE "DISTRESS MAGNITUDE"
        // We sum the deviations in the direction of Stress.
        // Anger = High HR + Low RMSSD + Low SDNN + Low pNN50 + High Ratio
        // We invert the sign for metrics that drop during stress.

        val distressVector = (zHr * 1.0) +       // HR goes UP
                (zRmssd * -1.0) +   // RMSSD goes DOWN
                (zSdnn * -1.5) +    // SDNN goes DOWN (Heavily weighted for anger)
                (zPnn50 * -1.0) +   // pNN50 goes DOWN
                (zRatio * 1.0)      // Ratio goes UP

        // 4. CALCULATE THE "EXERCISE MAGNITUDE"
        // Exercise = High HR + Low RMSSD + Moderate/High SDNN + Moderate Ratio

        val exerciseVector = (zHr * 1.0) +      // HR goes UP
                (zRmssd * -0.5) +  // RMSSD goes DOWN (but less than anger)
                (zSdnn * 0.5) +    // SDNN stays Neutral or goes UP (Mechanical noise)
                (zRatio * 0.2)     // Ratio goes UP slightly

        // 5. THE VERDICT (Comparison)

        println("ANALYSIS: DistressScore=${"%.2f".format(distressVector)} | ExerciseScore=${"%.2f".format(exerciseVector)}")

        return when {
            // A Distress score > 3.0 indicates statistically significant deviation (Sigma 3)
            // AND Distress must be clearly stronger than the Exercise signature
            distressVector > 3.5 && distressVector > exerciseVector -> MentalState.DISTRESS

            // If HR is high (zHr > 1.5) but it didn't trigger Distress
            zHr > 1.5 -> MentalState.EXCITEMENT

            else -> MentalState.CALM_OR_HAPPY
        }
    }
}
package com.example.mindshield.domain.analysis

import com.example.mindshield.model.HrvMetrics
import kotlin.math.abs

// 1. Define your Input Data Structure


enum class MentalState {
    NULL,
    CALM_OR_HAPPY,  // Low HR, High/Normal HRV
    EXCITEMENT,     // High HR, Moderate HRV (Positive Stress)
    DISTRESS        // High HR, Low HRV (Anger/Fear/Anxiety) -> THIS IS YOUR TARGET
}

object PhysiologicalAnalyzer {

    fun analyze(currentHr: Int, hrv: HrvMetrics): MentalState {
        // --- STEP 1: PRE-CALCULATIONS ---
        val lfHfRatio = if (hrv.hf > 0) hrv.lf / hrv.hf else 2.0

        // --- STEP 2: DEFINE "BAD" SIGNALS (Boolean Flags) ---

        // 1. RMSSD (Vagal Tone): < 20ms indicates strong vagal withdrawal
        val isVagalShutdown = hrv.rmssd < 20.0

        // 2. pNN50 (Parasympathetic Activity): < 3% means essentially zero relaxation response
        val isRelaxationGone = hrv.pnn50 < 3.0

        // 3. SDNN (Total Variability): < 30ms means the heart is beating "rigidly" (Metronomic)
        // This is crucial: Exercise often keeps SDNN higher (>40) due to mechanical variance.
        // Anger clamps it down (<30).
        val isHeartRigid = hrv.sdnn < 30.0

        // 4. LF/HF (Sympathetic Dominance): > 3.5 is a massive adrenaline spike
        val isAdrenalineSpike = lfHfRatio > 3.5

        // --- STEP 3: DECISION LOGIC ---

        // RULE 1: If HR is normal, you are safe.
        // Even if HRV is weird, low HR precludes "Anger" or "Exercise".
        if (currentHr < 90) {
            return MentalState.CALM_OR_HAPPY
        }

        // RULE 2: High Arousal State (HR > 90) - Is it Good or Bad?

        // We look for the "Triangle of Distress":
        // 1. Vagal Shutdown (Low RMSSD)
        // 2. Rigid Heart (Low SDNN) - Differentiates from Exercise
        // 3. Adrenaline Spike (High Ratio)

        val stressScore = listOf(
            isVagalShutdown,
            isRelaxationGone,
            isHeartRigid,
            isAdrenalineSpike
        ).count { it } // Count how many flags are TRUE

        return when {
            // If 3 or more flags are red, it's definitely Distress
            stressScore >= 3 -> MentalState.DISTRESS

            // Special Case: Even if score is 2, if the Heart is Rigid AND Vagus is Shutdown
            // It's likely mental stress, not exercise.
            isHeartRigid && isVagalShutdown -> MentalState.DISTRESS

            // Otherwise, it's high arousal but "Healthy" (Exercise/Excitement)
            else -> MentalState.EXCITEMENT
        }
    }
}
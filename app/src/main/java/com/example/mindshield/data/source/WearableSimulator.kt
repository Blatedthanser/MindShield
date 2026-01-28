package com.example.mindshield.data.source

import com.example.mindshield.model.HrvMetrics
import com.example.mindshield.model.WearableData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlin.random.Random

enum class SimState {
    RESTING,    // Low HR, High HRV
    EXERCISE,   // High HR, Moderate HRV (Healthy)
    STRESS_ANGER // High HR, CRASHED HRV (Unhealthy)
}

object WearableSimulator : IWearableSource {

    var isConnected = false
    private var currentScenario = MutableStateFlow(SimState.RESTING)
    private var internalHr = 70.0

    override fun connect() { isConnected = true }
    override fun disconnect() { isConnected = false }

    fun setScenario(state: SimState) {
        currentScenario.value = state
        println("SIMULATOR: Switched to $state")
    }

    private val flowScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val coldFlow = flow {
        while (true) {
            if (!isConnected) {
                delay(1000)
                continue
            }

            // 1. Target HR Logic
            val targetHr = when (currentScenario.value) {
                SimState.RESTING -> 65.0
                SimState.EXERCISE -> 130.0 // Exercise usually has higher HR than anger
                SimState.STRESS_ANGER -> 110.0 // Anger/Fear HR
            }

            // 2. Smooth HR Transition
            val diff = targetHr - internalHr
            internalHr += (diff * 0.1) + Random.nextDouble(-2.0, 2.0)
            internalHr = internalHr.coerceIn(50.0, 190.0)

            // 3. Generate 5-Parameter Metrics
            val metrics = generatePreciseMetrics(internalHr, currentScenario.value)

            emit(
                WearableData(
                    hr = internalHr.toInt(),
                    hrv = metrics
                )
            )

            delay(1000)
        }
    }

    private val _sharedData: SharedFlow<WearableData> = coldFlow.shareIn(
        scope = flowScope,
        started = SharingStarted.Lazily,
        replay = 1  // New subscribers get the latest value immediately
    )

    override fun observeData(): SharedFlow<WearableData> {
        return _sharedData
    }

    private fun generatePreciseMetrics(hr: Double, state: SimState): HrvMetrics {
        // Base RMSSD drops as HR rises
        val baseRmssd = 2500.0 / hr

        return when (state) {
            SimState.RESTING -> {
                // CALM: Everything is high and variable
                val rmssd = baseRmssd * Random.nextDouble(1.0, 1.3) // High Vagal Tone
                val sdnn = rmssd * 1.8  // High Variability
                val pnn50 = Random.nextDouble(15.0, 30.0) // High pNN50 (>15%)

                HrvMetrics(
                    rmssd = rmssd,
                    sdnn = sdnn,
                    pnn50 = pnn50,
                    lf = 400.0 + Random.nextDouble(0.0, 100.0),
                    hf = 300.0 + Random.nextDouble(0.0, 100.0) // Ratio ~1.3 (Balanced)
                )
            }

            SimState.EXERCISE -> {
                // EXERCISE: High HR, Low Vagal, BUT Moderate SDNN (Mechanical Pump Variance)
                val rmssd = baseRmssd * Random.nextDouble(0.9, 1.1) // Lower RMSSD
                val sdnn = rmssd * 2.0 // SDNN stays strictly higher than RMSSD during movement
                val pnn50 = Random.nextDouble(0.0, 3.0) // pNN50 drops off cliff

                // Sympathetic active, but HF still exists due to heavy breathing
                val hf = 100.0
                val lf = 250.0 // Ratio ~2.5

                HrvMetrics(
                    rmssd = rmssd.coerceAtLeast(15.0),
                    sdnn = sdnn.coerceAtLeast(35.0), // KEY: Keeps > 30.0
                    pnn50 = pnn50,
                    lf = lf,
                    hf = hf
                )
            }

            SimState.STRESS_ANGER -> {
                // ANGER: High HR, Vagal CRASH, Rigid Heart (Low SDNN)
                // We artificially crush the values to simulate the "Metronomic" heart of fear
                val rmssd = (baseRmssd * 0.4).coerceAtMost(18.0) // CRUSHED (< 20)
                val sdnn = rmssd * 1.1 // CRUSHED (< 30) - Very rigid
                val pnn50 = 0.0 // Zero relaxation

                // Adrenaline Spike
                val hf = 50.0
                val lf = 300.0 // Ratio ~6.0 (Massive Sympathetic)

                HrvMetrics(
                    rmssd = rmssd,
                    sdnn = sdnn,
                    pnn50 = pnn50,
                    lf = lf,
                    hf = hf
                )
            }
        }
    }
}
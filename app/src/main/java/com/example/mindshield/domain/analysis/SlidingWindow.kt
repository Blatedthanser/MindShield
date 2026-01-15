package com.example.mindshield.domain.analysis
import com.example.mindshield.model.WearableData
import java.util.ArrayDeque

class WindowedStateAnalyzer {

    // 1. CONFIGURATION
    private val windowDurationMs = 30 * 1000L // 30 Seconds
    private val minSamplesToStart = 5

    // 2. STATE BUFFER
    // We store a Pair: (Timestamp, The Decision)
    private val stateHistory = ArrayDeque<Pair<Long, MentalState>>()

    /**
     * 1. Calls PhysiologicalAnalyzer FIRST.
     * 2. Adds the result to history.
     * 3. Returns the dominant state (Majority Vote).
     */
    fun process(data: WearableData): MentalState {

        // --- STEP 1: Analyze Immediately (The Raw Classification) ---
        // We pass the full HRV object, so all 5 parameters are used correctly.
        val instantState = PhysiologicalAnalyzer.analyze(data.hr, data.hrv, UserBaseline)

        // --- STEP 2: Add Result to Sliding Window ---
        val now = data.timestamp
        stateHistory.addLast(now to instantState)

        // --- STEP 3: Prune Old History ---
        while (!stateHistory.isEmpty() && (now - stateHistory.first.first > windowDurationMs)) {
            stateHistory.removeFirst()
        }

        // --- STEP 4: Cold Start Check ---
        if (stateHistory.size < minSamplesToStart) {
            return MentalState.NULL
        }

        // --- STEP 5: The Majority Vote (Smoothing) ---
        // We count how many times each state appears in the window
        val voteCounts = stateHistory.groupingBy { it.second }.eachCount()

        // Find the state with the highest count
        // maxByOrNull returns the entry with max value
        val majorityState = voteCounts.maxByOrNull { it.value }?.key

        return majorityState ?: MentalState.NULL
    }

    fun reset() {
        stateHistory.clear()
    }
}
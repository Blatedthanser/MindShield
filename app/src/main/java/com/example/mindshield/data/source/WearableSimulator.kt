package com.example.mindshield.data.source

import com.example.mindshield.model.WearableData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class WearableSimulator : IWearableSource {

    private var isConnected = false

    override fun connect() {
        isConnected = true
    }

    override fun disconnect() {
        isConnected = false
    }

    override fun observeData(): Flow<WearableData> = flow {
        // Base values to start with
        var currentHr = 70
        var currentHrv = 50

        while (true) {
            if (!isConnected) {
                delay(1000)
                continue
            }

            // SIMULATION LOGIC:
            // 1. Slightly fluctuate HR (Human heart rates don't jump 60->120 instantly)
            // Add a random value between -2 and +3
            currentHr += Random.nextInt(-2, 3)
            // Clamp it so it doesn't go crazy (e.g., stay between 55 and 130)
            currentHr = currentHr.coerceIn(55, 130)

            // 2. Fluctuate HRV (Usually opposite of HR: High HR = Low HRV)
            // Just random noise for now
            currentHrv = (120 - currentHr) + Random.nextInt(-10, 10)
            currentHrv = currentHrv.coerceIn(10, 100)

            // Emit the new fake data
            emit(WearableData(heartRate = currentHr, hrv = currentHrv))

            // Wait 1 second before next reading
            delay(1000)
        }
    }
}
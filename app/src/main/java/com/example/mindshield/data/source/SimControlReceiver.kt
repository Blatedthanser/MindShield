package com.example.mindshield.data.source

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class SimControlReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // 1. Check if this is our specific action
        if (intent?.action == "com.myapp.action.SET_SIM_STATE") {

            // 2. Extract the "mode" string passed from terminal
            val mode = intent.getStringExtra("mode")?.uppercase()

            // 3. Map string to SimState
            val newState = when (mode) {
                "STRESS" -> SimState.STRESS_ANGER
                "EXERCISE" -> SimState.EXERCISE
                "REST" -> SimState.RESTING
                else -> null
            }

            // 4. Trigger the Simulator
            if (newState != null) {
                WearableSimulator.setScenario(newState)
                Toast.makeText(context, "Simulating: $mode", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
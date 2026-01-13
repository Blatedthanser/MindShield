package com.example.mindshield.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.mindshield.R
import com.example.mindshield.data.repository.CurrentData
import com.example.mindshield.data.source.IWearableSource
import com.example.mindshield.data.source.WearableSimulator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

// Import your data layer classes
// import com.example.mindshield.data.source.WearableManager

class MindShieldService : Service() {


    // 1. Initialize the interface (In the future, swap 'WearableSimulator()' with 'RealSdkWrapper()')
    private val wearableSource: IWearableSource = WearableSimulator()

    // Create a scope for background work
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    private val CHANNEL_ID = "mindshield_service_channel"

    override fun onCreate() {
        super.onCreate()
        println("MindShieldService: onCreate called (One-time setup)")

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("MindShieldService: onStartCommand called (Starting work)")

        // 1. Immediately turn into a Foreground Service
        startForeground(1, buildNotification())

        // 2. Connect to the band (Safe to call multiple times if your simulator handles it)
        wearableSource.connect()

        // 3. Start collecting data (Launch in our custom scope)
        serviceScope.launch {
            var lastUpdate = System.currentTimeMillis()
            wearableSource.observeData().collect { data ->
                val now = System.currentTimeMillis()
                // This runs in the background
                println("CORE SERVICE: HR=${data.hr} bpm")
                println("CORE SERVICE: HRV=${data.hrv}")
                // TODO: Feed this data into your 'PhysiologicalAnalyzer' here
                if (now - lastUpdate >= 5_000) { // ~5 seconds
                    CurrentData.updateData(data)
                    lastUpdate = now
                }

            }
        }

        // If the system kills the service due to low memory, restart it automatically
        return START_STICKY
    }

    private fun startDataCollection() {
        // Logic to connect to wristband
        println("MindShield: connecting to device...")

        // Pseudo-code for data flow:
        // wearableManager.connect()
        // wearableManager.onHeartRateReceived = { hr ->
        //      PhysiologicalAnalyzer.analyze(hr)
        // }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {
        val channelId = "mindshield_service_channel"
        val channelName = "MindShield Monitor"

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW // Low importance = no sound/popup
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("MindShield Active")
            .setContentText("Monitoring physiological data...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // We don't need to bind to UI activities for now
    }

    private fun createNotificationChannel() {
        // Notification Channels are only needed on Android O (API 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "MindShield Background Monitor", // Name visible to user in Settings
                NotificationManager.IMPORTANCE_LOW // Low = No sound/vibration
            ).apply {
                description = "Keeps the app connected to your wristband."
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MindShield Protection Active")
            .setContentText("Monitoring physiological data...")
            .setSmallIcon(android.R.drawable.ic_menu_myplaces) // Replace with your R.drawable.ic_shield
            .setOngoing(true) // User cannot swipe it away easily
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        wearableSource.disconnect()
        serviceScope.cancel() // Stop the loop
    }
}
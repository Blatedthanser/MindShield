package com.example.mindshield.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.mindshield.R
import com.example.mindshield.data.preferences.UserSettings
import com.example.mindshield.data.repository.DynamicDataToShieldPage
import com.example.mindshield.data.repository.OnboardingManager
import com.example.mindshield.data.source.IWearableSource
import com.example.mindshield.data.source.WearableSimulator
import com.example.mindshield.domain.analysis.MentalState
import com.example.mindshield.domain.analysis.PhysiologicalAnalyzer
import com.example.mindshield.domain.analysis.WindowedStateAnalyzer
import com.example.mindshield.domain.calibration.UserBaseline
import com.example.mindshield.ui.intervention.InterventionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MindShieldService : Service() {


    // 1. Initialize the interface (In the future, swap 'WearableSimulator()' with 'RealSdkWrapper()')
    private val wearableSource: IWearableSource = WearableSimulator

    // Create a scope for background work
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val CHANNEL_ID = "mindshield_service_channel"

    private lateinit var onboardingManager: OnboardingManager

    companion object{
        const val ACTION_RESPONSE = "analyzer.ACTION_RESPONSE"
        private var instance: MindShieldService? = null

        // 供外部调用的公开方法
        fun judgeOnResponse(result: String) {
            instance?.judgeOnResponse(result) ?: Log.e("MindShield", "服务未开启，无法启动任务")
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        instance = this
        Log.d("MindShield", "服务已连接，实例已注册")
        println("MindShieldService: onCreate called (One-time setup)")

        val settings = UserSettings(applicationContext)
        PhysiologicalAnalyzer.observeSensitivity(serviceScope, settings)

        // Connect to band
        wearableSource.connect()

        // Start collecting data (Launch in our custom scope)
        serviceScope.launch {
            val analyzer = WindowedStateAnalyzer()

            var lastUpdate = System.currentTimeMillis() - 4000
            var lastClassification = System.currentTimeMillis()

            onboardingManager = OnboardingManager(applicationContext)
            var triggerCount: Int = 0

            while (true) {
                if (!WearableSimulator.isConnected || !onboardingManager.isOnboardingCompleted.first()) {
                    // suspend until connection comes back
                    delay(1000)
                    continue  // restart loop check
                }
                wearableSource.observeData().collect { data ->
                    val now = System.currentTimeMillis()

                    val smoothedState = analyzer.process(data) //Pass data to SlidingWindow

                    println("CORE SERVICE: HR=${data.hr} | RawHRV=${data.hrv.rmssd.toInt()} | State=$smoothedState")

                    // 3. Only proceed if we have enough data (Not NULL)
                    if (smoothedState != MentalState.NULL) {

                        // Logic A: Trigger Intervention (Distress)
                        if (smoothedState == MentalState.DISTRESS && (now - lastClassification >= 60_000 * 0.05) && triggerCount < 2) {  // 5 minutes
                            println("CORE SERVICE: Distress detected. Starting classification...")
                            triggerCount ++
                            startTextClassification()
                            lastClassification = now
                        }
                        else if (now - lastClassification >= 60_000 * 0.2){
                            triggerCount = 0
                        }
                        // Logic B: Update UI (Throttled to 5 seconds)
                        if (now - lastUpdate >= 5_000) {
                            // Note: We send the 'Instant' HR (for display)
                            // but the 'Smoothed' State (for color/status)
                            DynamicDataToShieldPage.updateData(data.hr, smoothedState)
                            lastUpdate = now
                        }
                    }
                    else if (data.hr != 0){
                        if (now - lastUpdate >= 5_000) {
                            // Note: We send the 'Instant' HR (for display)
                            // but the 'Smoothed' State (for color/status)
                            DynamicDataToShieldPage.updateData(data.hr, MentalState.NULL)
                            lastUpdate = now
                        }
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("MindShieldService: onStartCommand called (Starting work)")

        // Immediately turn into a Foreground Service
        startForeground(1, buildNotification())

        // If the system kills the service due to low memory, restart it automatically
        return START_STICKY
    }

    private fun judgeOnResponse(result: String) {
        if (result == "非常负面 (Very Negative)") {
            Log.d("TAG","强干预")
        }
        else if (result == "负面 (Negative)") {
            Log.d("TAG","弱干预")
        }
    }
    private fun startTextClassification() {
        Log.d("MindShield", "尝试启动诊断...")
        MindShieldAccessibilityService.startDiagnosisFromActivity()
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
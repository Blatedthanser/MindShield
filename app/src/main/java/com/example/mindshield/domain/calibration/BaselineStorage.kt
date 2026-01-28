package com.example.mindshield.domain.calibration

import android.content.Context
import com.google.gson.Gson

class BaselineStorage(context: Context) {
    private val appContext = context.applicationContext

    private val prefs =
        appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()
    private val KEY_BASELINE = "user_baseline_data"

    // SAVE: Take data from the Singleton -> specific Data Class -> JSON -> Phone Storage
    fun saveBaseline() {
        val snapshot = BaselineSnapshot(
            hr = UserBaseline.hr,
            rmssd = UserBaseline.rmssd,
            sdnn = UserBaseline.sdnn,
            pnn50 = UserBaseline.pnn50,
            lf = UserBaseline.lf,
            hf = UserBaseline.hf,
            isCalibrated = UserBaseline.isCalibrated.value
        )

        val jsonString = gson.toJson(snapshot)

        prefs.edit()
            .putString(KEY_BASELINE, jsonString)
            .apply()
    }

    // LOAD: Phone Storage -> JSON -> Data Class -> Update Singleton
    fun loadBaseline() {
        val jsonString = prefs.getString(KEY_BASELINE, null)

        if (jsonString != null) {
            // Convert JSON back to the Snapshot object
            val snapshot = gson.fromJson(jsonString, BaselineSnapshot::class.java)

            // Update the Singleton variables
            UserBaseline.hr = snapshot.hr
            UserBaseline.rmssd = snapshot.rmssd
            UserBaseline.sdnn = snapshot.sdnn
            UserBaseline.pnn50 = snapshot.pnn50
            UserBaseline.lf = snapshot.lf
            UserBaseline.hf = snapshot.hf
            UserBaseline.setCalibrated(snapshot.isCalibrated)
        }
        else {

        }
    }

    // CLEAR
    fun clearBaseline() {
        prefs.edit().remove(KEY_BASELINE).apply()
    }
}
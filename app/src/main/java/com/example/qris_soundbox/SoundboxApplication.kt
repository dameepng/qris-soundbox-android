package com.example.qris_soundbox

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.qris_soundbox.utils.Constants

class SoundboxApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.FCM_CHANNEL_ID,
                Constants.FCM_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = Constants.FCM_CHANNEL_DESC

                // SILENT NOTIFICATION - No sound, no vibrate
                setSound(null, null) // ← Remove default sound
                enableVibration(false) // ← Disable vibration
                enableLights(false) // ← Disable LED (optional)

                // Keep importance HIGH for heads-up display
                importance = NotificationManager.IMPORTANCE_HIGH
            }

            val notificationManager = getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            notificationManager.createNotificationChannel(channel)
            android.util.Log.d("SoundboxApp", "Silent notification channel created")
        }
    }
}
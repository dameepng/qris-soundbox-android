package com.example.qris_soundbox.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            // Payment channel (HIGH priority)
            val paymentChannel = NotificationChannel(
                Constants.FCM_CHANNEL_ID,
                Constants.FCM_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = Constants.FCM_CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
            }

            notificationManager.createNotificationChannel(paymentChannel)
        }
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }
}
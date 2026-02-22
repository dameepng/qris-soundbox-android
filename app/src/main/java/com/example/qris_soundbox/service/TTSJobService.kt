package com.example.qris_soundbox.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService

class TTSJobService : JobIntentService() {

    companion object {
        private const val TAG = "TTSJobService"
        private const val JOB_ID = 1001

        fun enqueueWork(context: Context, amount: Int) {
            val intent = Intent().apply {
                putExtra("amount", amount)
            }
            enqueueWork(context, TTSJobService::class.java, JOB_ID, intent)
        }
    }

    private lateinit var ttsService: TTSService

    override fun onCreate() {
        super.onCreate()
        ttsService = TTSService(this)
        ttsService.initialize()

        // Wait for TTS init
        Thread.sleep(1000)
    }

    override fun onHandleWork(intent: Intent) {
        val amount = intent.getIntExtra("amount", 0)
        if (amount > 0) {
            Log.d(TAG, "Handling TTS for amount: $amount")
            ttsService.announcePayment(amount)

            // Keep service alive until TTS completes
            Thread.sleep(5000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsService.shutdown()
    }
}
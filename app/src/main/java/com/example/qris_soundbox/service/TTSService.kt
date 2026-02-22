package com.example.qris_soundbox.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.qris_soundbox.utils.Constants
import java.util.Locale

class TTSService(private val context: Context) {

    companion object {
        private const val TAG = "TTSService"
        private const val UTTERANCE_ID = "payment_announcement"
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val audioManager = context.getSystemService(
        Context.AUDIO_SERVICE
    ) as AudioManager

    // ─── Initialization ───────────────────────────────────

    fun initialize() {
        Log.d(TAG, "Initializing TTS...")
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("id", "ID"))

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Log.w(TAG, "Indonesian not supported, using default")
                    tts?.setLanguage(Locale.getDefault())
                }

                // Configure TTS settings
                tts?.setSpeechRate(Constants.TTS_SPEECH_RATE)
                tts?.setPitch(Constants.TTS_PITCH)

                // Set listener
                setupUtteranceListener()

                isInitialized = true
                Log.d(TAG, "✅ TTS initialized successfully")

            } else {
                Log.e(TAG, "❌ TTS initialization failed with status: $status")
            }
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d(TAG, "🔊 TTS started speaking")
                }

                override fun onDone(utteranceId: String?) {
                    Log.d(TAG, "✅ TTS finished speaking")
                    releaseAudioFocus()
                    restoreVolume()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "❌ TTS error for utterance: $utteranceId")
                    releaseAudioFocus()
                    restoreVolume()
                    playFallbackSound()
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.e(TAG, "❌ TTS error: $errorCode for utterance: $utteranceId")
                    releaseAudioFocus()
                    restoreVolume()
                    playFallbackSound()
                }
            }
        )
    }

    // ─── Main Announcement ────────────────────────────────

    fun announcePayment(amount: Int) {
        Log.d(TAG, "📢 announcePayment called with amount: $amount")
        Log.d(TAG, "   isInitialized: $isInitialized")

        if (!isInitialized) {
            Log.e(TAG, "❌ TTS not initialized yet, playing fallback sound")
            playFallbackSound()
            return
        }

        // Request audio focus
        requestAudioFocus()

        // Boost volume for better audibility
        boostVolume()

        // Build announcement text
        val text = buildAnnouncementText(amount)
        Log.d(TAG, "🔊 TTS Speaking: $text")

        // Speak
        val params = Bundle().apply {
            putString(
                TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                UTTERANCE_ID
            )
        }

        val speakResult = tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            params,
            UTTERANCE_ID
        )

        when (speakResult) {
            TextToSpeech.SUCCESS -> Log.d(TAG, "✅ TTS speak queued successfully")
            TextToSpeech.ERROR -> {
                Log.e(TAG, "❌ TTS speak failed")
                playFallbackSound()
            }
            else -> Log.w(TAG, "⚠️ TTS speak returned: $speakResult")
        }
    }

    // ─── Text Builder ─────────────────────────────────────

    private fun buildAnnouncementText(amount: Int): String {
        val amountText = convertAmountToWords(amount)
        return "Pembayaran $amountText rupiah, berhasil diterima"
    }

    private fun convertAmountToWords(amount: Int): String {
        return when {
            // Exact values (most common for QRIS)
            amount == 1_000 -> "seribu"
            amount == 2_000 -> "dua ribu"
            amount == 5_000 -> "lima ribu"
            amount == 10_000 -> "sepuluh ribu"
            amount == 15_000 -> "lima belas ribu"
            amount == 20_000 -> "dua puluh ribu"
            amount == 25_000 -> "dua puluh lima ribu"
            amount == 30_000 -> "tiga puluh ribu"
            amount == 50_000 -> "lima puluh ribu"
            amount == 75_000 -> "tujuh puluh lima ribu"
            amount == 100_000 -> "seratus ribu"
            amount == 150_000 -> "seratus lima puluh ribu"
            amount == 200_000 -> "dua ratus ribu"
            amount == 250_000 -> "dua ratus lima puluh ribu"
            amount == 500_000 -> "lima ratus ribu"
            amount == 1_000_000 -> "satu juta"

            // Dynamic conversion for other amounts
            amount >= 1_000_000 -> {
                val juta = amount / 1_000_000
                val sisaJuta = amount % 1_000_000
                val jutaText = if (juta == 1) "satu juta" else "$juta juta"

                if (sisaJuta == 0) jutaText
                else "$jutaText ${convertAmountToWords(sisaJuta)}"
            }

            amount >= 1_000 -> {
                val ribu = amount / 1_000
                val sisaRibu = amount % 1_000
                val ribuText = if (ribu == 1) "seribu" else "$ribu ribu"

                if (sisaRibu == 0) ribuText
                else "$ribuText $sisaRibu"
            }

            // Just read the number directly
            else -> amount.toString()
        }
    }

    // ─── Audio Management ─────────────────────────────────

    private var originalVolume = 0
    private var audioFocusRequest: AudioFocusRequest? = null

    private fun boostVolume() {
        try {
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = (maxVolume * 0.9).toInt()

            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                targetVolume,
                0
            )
            Log.d(TAG, "🔊 Volume boosted: $originalVolume → $targetVolume/$maxVolume")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to boost volume: ${e.message}")
        }
    }

    private fun restoreVolume() {
        try {
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                originalVolume,
                0
            )
            Log.d(TAG, "🔉 Volume restored: $originalVolume")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore volume: ${e.message}")
        }
    }

    private fun requestAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val focusRequest = AudioFocusRequest.Builder(
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .build()

                audioFocusRequest = focusRequest
                audioManager.requestAudioFocus(focusRequest)
                Log.d(TAG, "🎧 Audio focus requested")
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request audio focus: ${e.message}")
        }
    }

    private fun releaseAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let {
                    audioManager.abandonAudioFocusRequest(it)
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
            Log.d(TAG, "🎧 Audio focus released")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release audio focus: ${e.message}")
        }
    }

    // ─── Fallback Sound ───────────────────────────────────

    private fun playFallbackSound() {
        try {
            val notification = android.media.RingtoneManager.getDefaultUri(
                android.media.RingtoneManager.TYPE_NOTIFICATION
            )
            val ringtone = android.media.RingtoneManager.getRingtone(context, notification)
            ringtone?.play()
            Log.d(TAG, "🔔 Fallback sound played")
        } catch (e: Exception) {
            Log.e(TAG, "Fallback sound failed: ${e.message}")
        }
    }

    // ─── Cleanup ──────────────────────────────────────────

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
            Log.d(TAG, "🛑 TTSService shutdown complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error during shutdown: ${e.message}")
        }
    }
}
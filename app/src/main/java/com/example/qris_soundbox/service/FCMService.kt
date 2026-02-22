package com.example.qris_soundbox.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.qris_soundbox.R
import com.example.qris_soundbox.data.repository.MerchantRepository
import com.example.qris_soundbox.data.repository.QRISRepository
import com.example.qris_soundbox.data.repository.TransactionRepository
import com.example.qris_soundbox.ui.main.MainActivity
import com.example.qris_soundbox.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FCMService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val WAKE_LOCK_DURATION = 20_000L // 20 seconds
    }

    private lateinit var ttsService: TTSService
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var qrisRepository: QRISRepository
    private lateinit var merchantRepository: MerchantRepository

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "FCMService created")

        // Initialize TTS service
        ttsService = TTSService(this)
        ttsService.initialize()

        // Wait for TTS to initialize properly
        Thread.sleep(800)

        // Initialize repositories
        transactionRepository = TransactionRepository(this)
        qrisRepository = QRISRepository(this)
        merchantRepository = MerchantRepository(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received: ${message.data}")

        // ONLY handle data messages (not notification messages)
        if (message.data.isEmpty()) {
            Log.w(TAG, "Empty data payload, ignoring")
            return
        }

        val data = message.data
        val type = data["type"] ?: return

        when (type) {
            "payment" -> handlePaymentMessage(data)
            else -> Log.w(TAG, "Unknown message type: $type")
        }
    }

    private fun handlePaymentMessage(data: Map<String, String>) {
        val status = data["status"] ?: return
        val amountStr = data["amount"] ?: return
        val transactionId = data["transaction_id"] ?: return
        val orderId = data["order_id"]
        val customerName = data["customer_name"]
        val amount = amountStr.toIntOrNull() ?: return

        Log.d(TAG, "Payment received: amount=$amount, status=$status, txId=$transactionId")

        if (status != Constants.STATUS_SUCCESS) {
            Log.d(TAG, "Payment status is not success: $status")
            return
        }

        val wakeLock = acquireWakeLock()

        try {
            // 1. Play custom sound effect + TTS sequence
            Log.d(TAG, "Playing sound effect + TTS for amount: $amount")
            Handler(Looper.getMainLooper()).post {
                playCustomSoundThenTTS(amount)
            }

            // 2. Show notification (without default sound)
            showPaymentNotification(amount, transactionId)

            // 3. Save to database
            CoroutineScope(Dispatchers.IO).launch {
                saveTransaction(
                    transactionId = transactionId,
                    amount = amount,
                    orderId = orderId,
                    customerName = customerName
                )

                orderId?.let {
                    qrisRepository.updateQRISPaid(
                        orderId = it,
                        transactionId = transactionId
                    )
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Handle payment error: ${e.message}", e)
        } finally {
            Handler(Looper.getMainLooper()).postDelayed({
                if (wakeLock.isHeld) {
                    wakeLock.release()
                    Log.d(TAG, "Wake lock released")
                }
            }, WAKE_LOCK_DURATION)
        }
    }

    private fun playCustomSoundThenTTS(amount: Int) {
        try {
            Log.d(TAG, "🔊 Playing custom notification sound at max volume...")

            // Get audio manager
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

            // Save original volume
            val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

            // Set MUSIC stream to 100% (MediaPlayer uses MUSIC stream)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)

            Log.d(TAG, "Volume set to MAX: $maxVolume (was $originalVolume)")

            // Create MediaPlayer with MUSIC stream
            val mediaPlayer = MediaPlayer.create(this, R.raw.payment_sound).apply {
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setVolume(1.0f, 1.0f) // Left & Right channel max
            }

            mediaPlayer?.setOnCompletionListener {
                it.release()

                // Restore original volume
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
                Log.d(TAG, "✅ Custom sound completed, volume restored to $originalVolume")
                Log.d(TAG, "Starting TTS...")

                // AFTER sound effect selesai, baru TTS bicara
                Handler(Looper.getMainLooper()).postDelayed({
                    ttsService.announcePayment(amount)
                }, 300) // Delay 300ms sebelum TTS mulai
            }

            mediaPlayer?.setOnErrorListener { mp, what, extra ->
                Log.e(TAG, "❌ MediaPlayer error: what=$what, extra=$extra")
                mp.release()

                // Restore volume on error
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)

                // Kalau sound effect error, langsung TTS saja
                ttsService.announcePayment(amount)
                true
            }

            mediaPlayer?.start()

        } catch (e: Exception) {
            Log.e(TAG, "Error playing custom sound: ${e.message}", e)

            // Fallback: langsung TTS kalau sound effect gagal
            ttsService.announcePayment(amount)
        }
    }

    private fun showPaymentNotification(amount: Int, txId: String) {
        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedAmount = formatRupiah(amount)

        val notification = NotificationCompat.Builder(this, Constants.FCM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("✅ Pembayaran Diterima")
            .setContentText("Pembayaran sebesar Rp $formattedAmount telah berhasil diterima")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Pembayaran sebesar Rp $formattedAmount telah berhasil diterima")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(null) // ← EXPLICITLY set to null (no sound)
            .setVibrate(null) // ← EXPLICITLY set to null (no vibrate)
            .setDefaults(0) // ← No defaults
            .setSilent(true) // ← Silent notification (Android 8+)
            .build()

        notificationManager.notify(txId.hashCode(), notification)
        Log.d(TAG, "Silent notification shown for txId: $txId")
    }

    private suspend fun saveTransaction(
        transactionId: String,
        amount: Int,
        orderId: String?,
        customerName: String?
    ) {
        try {
            transactionRepository.insertTransactionFromFCM(
                transactionId = transactionId,
                amount = amount,
                orderId = orderId,
                customerName = customerName
            )
            Log.d(TAG, "Transaction saved: $transactionId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save transaction: ${e.message}")
        }
    }

    private fun acquireWakeLock(): PowerManager.WakeLock {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Soundbox::FCMWakeLock"
        )
        wakeLock.acquire(WAKE_LOCK_DURATION)
        Log.d(TAG, "Wake lock acquired for $WAKE_LOCK_DURATION ms")
        return wakeLock
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")

        // Update token di backend
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val merchantId = merchantRepository.getMerchantId() ?: return@launch
                val apiKey = merchantRepository.getApiKey() ?: return@launch

                merchantRepository.updateFCMToken(
                    apiKey = apiKey,
                    merchantId = merchantId,
                    newToken = token
                )
                Log.d(TAG, "FCM token updated on server")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update FCM token: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "FCMService destroyed")
    }

    private fun formatRupiah(amount: Int): String {
        return String.format("%,d", amount).replace(',', '.')
    }
}
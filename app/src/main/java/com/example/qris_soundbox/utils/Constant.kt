package com.example.qris_soundbox.utils

object Constants {
    // API Configuration
    const val BASE_URL_DEV = "http://10.0.2.2:3000/"
    const val BASE_URL_PROD = "https://qris-soundbox-production.up.railway.app/"
    const val BASE_URL = BASE_URL_PROD

    // API Endpoints
    const val ENDPOINT_QRIS_GENERATE = "api/qris/generate"
    const val ENDPOINT_QRIS_STATUS = "api/qris/status/{order_id}"
    const val ENDPOINT_MERCHANT_REGISTER = "api/merchant/register"
    const val ENDPOINT_FCM_TOKEN = "api/merchant/fcm-token"

    // FCM
    const val FCM_CHANNEL_ID = "payment_channel"
    const val FCM_CHANNEL_NAME = "Payment Notifications"
    const val FCM_CHANNEL_DESC = "Notifikasi pembayaran QRIS"

    // QRIS Configuration
    const val QRIS_EXPIRY_MINUTES = 5L
    const val QRIS_MIN_AMOUNT = 1000
    const val QRIS_MAX_AMOUNT = 10_000_000

    // TTS Configuration
    const val TTS_LANGUAGE = "id-ID"
    const val TTS_SPEECH_RATE = 0.85f
    const val TTS_PITCH = 1.0f
    const val TTS_WAKE_LOCK_TIMEOUT = 10_000L // 10 seconds

    // Database
    const val DATABASE_NAME = "soundbox_db"
    const val DATABASE_VERSION = 1

    // SharedPreferences
    const val PREF_NAME = "soundbox_prefs"
    const val PREF_MERCHANT_ID = "merchant_id"
    const val PREF_API_KEY = "api_key"
    const val PREF_FCM_TOKEN = "fcm_token"
    const val PREF_IS_REGISTERED = "is_registered"

    // Payment Status
    const val STATUS_PENDING = "pending"
    const val STATUS_SUCCESS = "success"
    const val STATUS_FAILED = "failed"
    const val STATUS_EXPIRED = "expired"
}
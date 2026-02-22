package com.example.qris_soundbox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "merchant_settings")
data class MerchantSettings(
    @PrimaryKey
    val merchantId: String,

    @ColumnInfo(name = "fcm_token")
    val fcmToken: String,

    @ColumnInfo(name = "api_key")
    val apiKey: String,

    @ColumnInfo(name = "qris_static")
    val qrisStatic: String,

    @ColumnInfo(name = "merchant_name")
    val merchantName: String,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "tts_enabled")
    val ttsEnabled: Boolean = true,

    @ColumnInfo(name = "tts_volume")
    val ttsVolume: Float = 1.0f,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

package com.example.qris_soundbox.data.remote.dto

import com.google.gson.annotations.SerializedName

// Request: Register Merchant
data class MerchantRegisterRequest(
    @SerializedName("merchant_id")
    val merchantId: String,

    @SerializedName("merchant_name")
    val merchantName: String,

    @SerializedName("fcm_token")
    val fcmToken: String,

    @SerializedName("phone_number")
    val phoneNumber: String? = null
)

// Response: Register Merchant
data class MerchantRegisterResponse(
    @SerializedName("merchant_id")
    val merchantId: String,

    @SerializedName("api_key")
    val apiKey: String,

    @SerializedName("qris_static")
    val qrisStatic: String
)

// Request: Update FCM Token
data class UpdateFCMTokenRequest(
    @SerializedName("merchant_id")
    val merchantId: String,

    @SerializedName("fcm_token")
    val fcmToken: String
)
package com.example.qris_soundbox.data.remote.dto

import com.google.gson.annotations.SerializedName

// Request: Generate QRIS Dinamis
data class QRISGenerateRequest(
    @SerializedName("merchant_id")
    val merchantId: String,

    @SerializedName("amount")
    val amount: Int,

    @SerializedName("description")
    val description: String? = null
)

// Response: Generate QRIS Dinamis
data class QRISGenerateResponse(
    @SerializedName("qris_id")
    val qrisId: String,

    @SerializedName("order_id")
    val orderId: String,

    @SerializedName("amount")
    val amount: Int,

    @SerializedName("qris_string")
    val qrisString: String,

    @SerializedName("expires_at")
    val expiresAt: String, // ISO8601 format

    @SerializedName("status")
    val status: String
)

// Response: Check QRIS Status
data class QRISStatusResponse(
    @SerializedName("order_id")
    val orderId: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("amount")
    val amount: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("paid_at")
    val paidAt: String? = null
)

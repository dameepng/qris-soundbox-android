package com.example.qris_soundbox.data.remote.dto

import com.google.gson.annotations.SerializedName

// Generic wrapper untuk semua API response
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("code")
    val code: String? = null,

    @SerializedName("data")
    val data: T? = null
)

package com.example.qris_soundbox.data.remote.api

import com.example.qris_soundbox.data.remote.dto.ApiResponse
import com.example.qris_soundbox.data.remote.dto.MerchantRegisterRequest
import com.example.qris_soundbox.data.remote.dto.MerchantRegisterResponse
import com.example.qris_soundbox.data.remote.dto.QRISGenerateRequest
import com.example.qris_soundbox.data.remote.dto.QRISGenerateResponse
import com.example.qris_soundbox.data.remote.dto.QRISStatusResponse
import com.example.qris_soundbox.data.remote.dto.UpdateFCMTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SoundboxApi {

    // ─── QRIS Endpoints ───────────────────────────────────

    @POST("api/qris/generate")
    suspend fun generateQRIS(
        @Header("X-API-Key") apiKey: String,
        @Body request: QRISGenerateRequest
    ): Response<ApiResponse<QRISGenerateResponse>>

    @GET("api/qris/status/{order_id}")
    suspend fun checkQRISStatus(
        @Header("X-API-Key") apiKey: String,
        @Path("order_id") orderId: String
    ): Response<ApiResponse<QRISStatusResponse>>

    @POST("api/qris/cancel")
    suspend fun cancelQRIS(
        @Header("X-API-Key") apiKey: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Unit>>

    // ─── Merchant Endpoints ───────────────────────────────

    @POST("api/merchant/register")
    suspend fun registerMerchant(
        @Body request: MerchantRegisterRequest
    ): Response<ApiResponse<MerchantRegisterResponse>>

    @PUT("api/merchant/fcm-token")
    suspend fun updateFCMToken(
        @Header("X-API-Key") apiKey: String,
        @Body request: UpdateFCMTokenRequest
    ): Response<ApiResponse<Unit>>

    // ─── Health Check ─────────────────────────────────────

    @GET("health")
    suspend fun healthCheck(): Response<ApiResponse<Unit>>
}
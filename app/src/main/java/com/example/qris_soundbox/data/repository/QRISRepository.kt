package com.example.qris_soundbox.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.qris_soundbox.data.local.database.SoundboxDatabase
import com.example.qris_soundbox.data.local.entity.QRISHistory
import com.example.qris_soundbox.data.remote.api.RetrofitClient
import com.example.qris_soundbox.data.remote.dto.QRISGenerateRequest
import com.example.qris_soundbox.utils.Constants
import com.example.qris_soundbox.utils.toRupiah
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class QRISRepository(context: Context) {

    private val qrisDao = SoundboxDatabase
        .getInstance(context)
        .qrisDao()

    private val api = RetrofitClient.api

    // ─── Remote Operations ────────────────────────────────

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun generateDynamicQRIS(
        apiKey: String,
        merchantId: String,
        amount: Int,
        description: String? = null
    ): Result<QRISHistory> {

        // Validate amount
        if (amount < Constants.QRIS_MIN_AMOUNT) {
            return Result.failure(
                ValidationException("Minimal pembayaran Rp ${Constants.QRIS_MIN_AMOUNT.toRupiah()}")
            )
        }
        if (amount > Constants.QRIS_MAX_AMOUNT) {
            return Result.failure(
                ValidationException("Maksimal pembayaran Rp ${Constants.QRIS_MAX_AMOUNT.toRupiah()}")
            )
        }

        return try {
            val response = api.generateQRIS(
                apiKey = apiKey,
                request = QRISGenerateRequest(
                    merchantId = merchantId,
                    amount = amount,
                    description = description
                )
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!

                // Parse expires_at dari ISO8601 string
                val expiresAt = try {
                    // Format: 2026-02-18T06:35:37.643Z
                    val isoFormat = java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        java.util.Locale.US
                    )
                    isoFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    isoFormat.parse(data.expiresAt)?.time
                        ?: (System.currentTimeMillis() + Constants.QRIS_EXPIRY_MINUTES * 60 * 1000)
                } catch (e: Exception) {
                    // Fallback: 5 minutes from now
                    System.currentTimeMillis() + Constants.QRIS_EXPIRY_MINUTES * 60 * 1000
                }

                // Create local entity
                val qrisHistory = QRISHistory(
                    qrisId = data.qrisId,
                    orderId = data.orderId,
                    amount = data.amount,
                    qrisString = data.qrisString,
                    status = Constants.STATUS_PENDING,
                    createdAt = System.currentTimeMillis(),
                    expiresAt = expiresAt
                )

                // Save to local database
                qrisDao.insert(qrisHistory)

                Result.success(qrisHistory)

            } else {
                val errorMessage = response.body()?.error
                    ?: response.body()?.message
                    ?: "Gagal generate QRIS: ${response.code()}"
                Result.failure(ApiException(errorMessage))
            }

        } catch (e: IOException) {
            Result.failure(NetworkException("Tidak ada koneksi internet", e))
        } catch (e: Exception) {
            // Log untuk debug
            android.util.Log.e("QRISRepository", "Generate QRIS error", e)
            Result.failure(UnknownException("Gagal generate QRIS: ${e.message}", e))
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun cancelQRIS(
        apiKey: String,
        orderId: String
    ): Result<Unit> {
        return try {
            val response = api.cancelQRIS(
                apiKey = apiKey,
                body = mapOf("order_id" to orderId)
            )

            if (response.isSuccessful) {
                // Update local status
                qrisDao.updateStatus(
                    orderId = orderId,
                    status = Constants.STATUS_EXPIRED,
                    paidAt = 0L,
                    txId = ""
                )
                Result.success(Unit)
            } else {
                Result.failure(ApiException("Gagal cancel QRIS"))
            }

        } catch (e: IOException) {
            Result.failure(NetworkException())
        } catch (e: Exception) {
            Result.failure(UnknownException())
        }
    }

    // ─── Local Operations ─────────────────────────────────

    fun getRecentQRIS(): Flow<List<QRISHistory>> {
        return qrisDao.getRecentQRIS()
    }

    fun getActiveQRIS(): Flow<List<QRISHistory>> {
        return qrisDao.getActiveQRIS(System.currentTimeMillis())
    }

    suspend fun updateQRISPaid(
        orderId: String,
        transactionId: String
    ) {
        qrisDao.updateStatus(
            orderId = orderId,
            status = Constants.STATUS_SUCCESS,
            paidAt = System.currentTimeMillis(),
            txId = transactionId
        )
    }

    suspend fun expireOldQRIS() {
        qrisDao.expireOldQRIS(System.currentTimeMillis())
    }
}
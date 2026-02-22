package com.example.qris_soundbox.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.qris_soundbox.data.local.database.SoundboxDatabase
import com.example.qris_soundbox.data.local.entity.MerchantSettings
import com.example.qris_soundbox.data.remote.api.RetrofitClient
import com.example.qris_soundbox.data.remote.dto.MerchantRegisterRequest
import com.example.qris_soundbox.data.remote.dto.UpdateFCMTokenRequest
import com.example.qris_soundbox.utils.Constants
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class MerchantRepository(private val context: Context) {

    private val merchantDao = SoundboxDatabase
        .getInstance(context)
        .merchantDao()

    private val api = RetrofitClient.api

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREF_NAME,
        Context.MODE_PRIVATE
    )

    // ─── SharedPreferences ────────────────────────────────

    fun getMerchantId(): String? {
        return prefs.getString(Constants.PREF_MERCHANT_ID, null)
    }

    fun getApiKey(): String? {
        return prefs.getString(Constants.PREF_API_KEY, null)
    }

    fun getFCMToken(): String? {
        return prefs.getString(Constants.PREF_FCM_TOKEN, null)
    }

    fun isRegistered(): Boolean {
        return prefs.getBoolean(Constants.PREF_IS_REGISTERED, false)
    }

    private fun saveToPrefs(
        merchantId: String,
        apiKey: String,
        fcmToken: String
    ) {
        prefs.edit()
            .putString(Constants.PREF_MERCHANT_ID, merchantId)
            .putString(Constants.PREF_API_KEY, apiKey)
            .putString(Constants.PREF_FCM_TOKEN, fcmToken)
            .putBoolean(Constants.PREF_IS_REGISTERED, true)
            .apply()
    }

    // ─── Remote Operations ────────────────────────────────

    suspend fun registerMerchant(
        merchantId: String,
        merchantName: String,
        fcmToken: String,
        phoneNumber: String? = null
    ): Result<MerchantSettings> {
        return try {
            val response = api.registerMerchant(
                request = MerchantRegisterRequest(
                    merchantId = merchantId,
                    merchantName = merchantName,
                    fcmToken = fcmToken,
                    phoneNumber = phoneNumber
                )
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!

                val settings = MerchantSettings(
                    merchantId = merchantId,
                    fcmToken = fcmToken,
                    apiKey = data.apiKey,
                    qrisStatic = data.qrisStatic,
                    merchantName = merchantName
                )

                // Save to local database
                merchantDao.insert(settings)

                // Save to SharedPreferences
                saveToPrefs(merchantId, data.apiKey, fcmToken)

                Result.success(settings)

            } else {
                val errorMessage = response.body()?.error
                    ?: "Gagal mendaftar merchant: ${response.code()}"
                Result.failure(ApiException(errorMessage))
            }

        } catch (e: IOException) {
            Result.failure(NetworkException("Koneksi bermasalah", e))
        } catch (e: Exception) {
            Result.failure(UnknownException("Gagal mendaftar merchant", e))
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun updateFCMToken(
        apiKey: String,
        merchantId: String,
        newToken: String
    ): Result<Unit> {
        return try {
            val response = api.updateFCMToken(
                apiKey = apiKey,
                request = UpdateFCMTokenRequest(
                    merchantId = merchantId,
                    fcmToken = newToken
                )
            )

            if (response.isSuccessful) {
                // Update locally
                merchantDao.updateFCMToken(merchantId, newToken)
                prefs.edit()
                    .putString(Constants.PREF_FCM_TOKEN, newToken)
                    .apply()
                Result.success(Unit)
            } else {
                Result.failure(ApiException("Gagal update FCM token"))
            }

        } catch (e: IOException) {
            Result.failure(NetworkException())
        } catch (e: Exception) {
            Result.failure(UnknownException())
        }
    }

    // ─── Local Operations ─────────────────────────────────

    fun getMerchantSettings(): Flow<MerchantSettings?> {
        return merchantDao.getMerchantSettings()
    }

    suspend fun getMerchantSettingsOnce(): MerchantSettings? {
        return merchantDao.getMerchantSettingsOnce()
    }

    suspend fun updateTTSEnabled(
        merchantId: String,
        enabled: Boolean
    ) {
        merchantDao.updateTTSEnabled(merchantId, enabled)
    }

    suspend fun updateTTSVolume(
        merchantId: String,
        volume: Float
    ) {
        merchantDao.updateTTSVolume(merchantId, volume)
    }
}
package com.example.qris_soundbox.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.qris_soundbox.data.local.entity.MerchantSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantDao {

    @Query("SELECT * FROM merchant_settings LIMIT 1")
    fun getMerchantSettings(): Flow<MerchantSettings?>

    @Query("SELECT * FROM merchant_settings LIMIT 1")
    suspend fun getMerchantSettingsOnce(): MerchantSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: MerchantSettings)

    @Update
    suspend fun update(settings: MerchantSettings)

    @Query("UPDATE merchant_settings SET fcm_token = :token WHERE merchantId = :merchantId")
    suspend fun updateFCMToken(merchantId: String, token: String)

    @Query("UPDATE merchant_settings SET tts_enabled = :enabled WHERE merchantId = :merchantId")
    suspend fun updateTTSEnabled(merchantId: String, enabled: Boolean)

    @Query("UPDATE merchant_settings SET tts_volume = :volume WHERE merchantId = :merchantId")
    suspend fun updateTTSVolume(merchantId: String, volume: Float)

    @Query("UPDATE merchant_settings SET is_active = :isActive WHERE merchantId = :merchantId")
    suspend fun updateActiveStatus(merchantId: String, isActive: Boolean)

    @Delete
    suspend fun delete(settings: MerchantSettings)

    @Query("DELETE FROM merchant_settings")
    suspend fun deleteAll()
}
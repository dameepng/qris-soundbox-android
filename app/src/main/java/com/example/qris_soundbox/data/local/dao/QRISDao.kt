package com.example.qris_soundbox.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.qris_soundbox.data.local.entity.QRISHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface QRISDao {

    @Query("SELECT * FROM qris_history ORDER BY created_at DESC LIMIT 10")
    fun getRecentQRIS(): Flow<List<QRISHistory>>

    @Query("SELECT * FROM qris_history WHERE qrisId = :qrisId")
    suspend fun getQRISById(qrisId: String): QRISHistory?

    @Query("SELECT * FROM qris_history WHERE order_id = :orderId")
    fun getQRISByOrderIdFlow(orderId: String): Flow<QRISHistory?>

    @Query("SELECT * FROM qris_history WHERE status = 'pending' AND expires_at > :currentTime")
    fun getActiveQRIS(currentTime: Long): Flow<List<QRISHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(qris: QRISHistory)

    @Update
    suspend fun update(qris: QRISHistory)

    @Query("UPDATE qris_history SET status = :status, paid_at = :paidAt, transaction_id = :txId WHERE order_id = :orderId")
    suspend fun updateStatus(orderId: String, status: String, paidAt: Long, txId: String)

    @Query("UPDATE qris_history SET status = 'expired' WHERE status = 'pending' AND expires_at < :currentTime")
    suspend fun expireOldQRIS(currentTime: Long)

    @Delete
    suspend fun delete(qris: QRISHistory)

    @Query("DELETE FROM qris_history WHERE created_at < :olderThan")
    suspend fun deleteOld(olderThan: Long)
}
package com.example.qris_soundbox.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.qris_soundbox.data.local.entity.PaymentTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<PaymentTransaction>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getTransactionsToday(startOfDay: Long): Flow<List<PaymentTransaction>>

    @Query("SELECT * FROM transactions WHERE transactionId = :id")
    suspend fun getTransactionById(id: String): PaymentTransaction?

    @Query("SELECT SUM(amount) FROM transactions WHERE timestamp >= :startOfDay AND status = 'success'")
    fun getDailyTotal(startOfDay: Long): Flow<Int?>

    @Query("SELECT COUNT(*) FROM transactions WHERE timestamp >= :startOfDay AND status = 'success'")
    fun getDailyCount(startOfDay: Long): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: PaymentTransaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<PaymentTransaction>)

    @Update
    suspend fun update(transaction: PaymentTransaction)

    @Delete
    suspend fun delete(transaction: PaymentTransaction)

    @Query("DELETE FROM transactions WHERE timestamp < :olderThan")
    suspend fun deleteOldTransactions(olderThan: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
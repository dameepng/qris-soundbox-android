package com.example.qris_soundbox.data.repository

import android.content.Context
import com.example.qris_soundbox.data.local.database.SoundboxDatabase
import com.example.qris_soundbox.data.local.entity.PaymentTransaction  // ← Update import
import com.example.qris_soundbox.utils.Constants
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TransactionRepository(context: Context) {

    private val transactionDao = SoundboxDatabase
        .getInstance(context)
        .transactionDao()

    // ─── Read Operations ──────────────────────────────────

    fun getAllTransactions(): Flow<List<PaymentTransaction>> {
        return transactionDao.getAllTransactions()
    }

    fun getTodayTransactions(): Flow<List<PaymentTransaction>> {
        return transactionDao.getTransactionsToday(getStartOfDay())
    }

    fun getDailyTotal(): Flow<Int?> {
        return transactionDao.getDailyTotal(getStartOfDay())
    }

    fun getDailyCount(): Flow<Int?> {
        return transactionDao.getDailyCount(getStartOfDay())
    }

    suspend fun getTransactionById(id: String): PaymentTransaction? {
        return transactionDao.getTransactionById(id)
    }

    // ─── Write Operations ─────────────────────────────────

    suspend fun insertTransaction(transaction: PaymentTransaction) {
        transactionDao.insert(transaction)
    }

    suspend fun insertTransactionFromFCM(
        transactionId: String,
        amount: Int,
        orderId: String? = null,
        customerName: String? = null
    ) {
        val transaction = PaymentTransaction(  // ← Update class name
            transactionId = transactionId,
            amount = amount,
            status = Constants.STATUS_SUCCESS,
            paymentMethod = "qris",
            customerName = customerName,
            timestamp = System.currentTimeMillis(),
            orderId = orderId,
            isSynced = true
        )
        transactionDao.insert(transaction)
    }

    suspend fun deleteOldTransactions(daysOld: Int = 30) {
        val cutoff = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        transactionDao.deleteOldTransactions(cutoff)
    }

    // ─── Helper ───────────────────────────────────────────

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
package com.example.qris_soundbox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class PaymentTransaction(  // ← Rename dari Transaction
    @PrimaryKey
    val transactionId: String,

    val amount: Int,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String = "qris",

    @ColumnInfo(name = "customer_name")
    val customerName: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "order_id")
    val orderId: String? = null,

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
)
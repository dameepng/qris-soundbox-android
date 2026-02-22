package com.example.qris_soundbox.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qris_history")
data class QRISHistory(
    @PrimaryKey
    val qrisId: String,

    @ColumnInfo(name = "order_id")
    val orderId: String,

    val amount: Int,

    @ColumnInfo(name = "qris_string")
    val qrisString: String,

    val status: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "expires_at")
    val expiresAt: Long,

    @ColumnInfo(name = "paid_at")
    val paidAt: Long? = null,

    @ColumnInfo(name = "transaction_id")
    val transactionId: String? = null
)

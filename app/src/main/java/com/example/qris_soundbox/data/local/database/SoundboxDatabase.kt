package com.example.qris_soundbox.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.qris_soundbox.data.local.dao.MerchantDao
import com.example.qris_soundbox.data.local.dao.QRISDao
import com.example.qris_soundbox.data.local.dao.TransactionDao
import com.example.qris_soundbox.data.local.entity.MerchantSettings
import com.example.qris_soundbox.data.local.entity.PaymentTransaction  // ← Update import
import com.example.qris_soundbox.data.local.entity.QRISHistory
import com.example.qris_soundbox.utils.Constants

@Database(
    entities = [
        PaymentTransaction::class,  // ← Update class name
        MerchantSettings::class,
        QRISHistory::class
    ],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
abstract class SoundboxDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun merchantDao(): MerchantDao
    abstract fun qrisDao(): QRISDao

    companion object {
        @Volatile
        private var INSTANCE: SoundboxDatabase? = null

        fun getInstance(context: Context): SoundboxDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SoundboxDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
package com.example.qris_soundbox.utils

import android.content.Context
import android.widget.Toast
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Toast Extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// Currency Formatter
fun Int.toRupiah(): String {
    return NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        .format(this)
        .replace("Rp", "Rp ")
}

fun Int.toRupiahWithoutSymbol(): String {
    return NumberFormat.getNumberInstance(Locale("id", "ID"))
        .format(this)
}

// Date/Time Formatter
fun Long.toDateString(pattern: String = "dd MMM yyyy, HH:mm"): String {
    val sdf = SimpleDateFormat(pattern, Locale("id", "ID"))
    return sdf.format(Date(this))
}

fun Long.toTimeString(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale("id", "ID"))
    return sdf.format(Date(this))
}

// String to Amount
fun String.toAmount(): Int {
    return this.replace(".", "")
        .replace(",", "")
        .replace("Rp", "")
        .replace(" ", "")
        .toIntOrNull() ?: 0
}

// Validation
fun String.isValidAmount(): Boolean {
    val amount = this.toAmount()
    return amount >= Constants.QRIS_MIN_AMOUNT && amount <= Constants.QRIS_MAX_AMOUNT
}
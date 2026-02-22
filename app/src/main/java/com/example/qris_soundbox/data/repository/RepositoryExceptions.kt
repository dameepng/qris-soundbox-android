package com.example.qris_soundbox.data.repository

/**
 * Exception thrown when QRIS generation validation fails (e.g., amount out of range).
 */
class ValidationException(message: String) : Exception(message)

/**
 * Exception thrown for generic or unknown errors in repository operations.
 */
class UnknownException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

/**
 * Custom exception for API errors, including an optional status or message.
 */
class ApiException(val errorMessage: String) : Exception(errorMessage)

/**
 * Exception thrown when a network error occurs (IOException wrapper).
 */
class NetworkException(message: String? = "Tidak ada koneksi internet", cause: Throwable? = null) : Exception(message, cause)

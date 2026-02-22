package com.example.qris_soundbox.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.qris_soundbox.data.repository.MerchantRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val merchantName: String = "",
    val phoneNumber: String = "",
    val error: String? = null,
    val isSuccess: Boolean = false
)

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val merchantRepository = MerchantRepository(application)

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateMerchantName(name: String) {
        _uiState.update { it.copy(merchantName = name, error = null) }
    }

    fun updatePhoneNumber(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone, error = null) }
    }

    fun registerMerchant() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val merchantName = _uiState.value.merchantName.trim()
            val phoneNumber = _uiState.value.phoneNumber.trim()

            // Validation
            if (merchantName.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Nama merchant tidak boleh kosong"
                    )
                }
                return@launch
            }

            if (merchantName.length < 3) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Nama merchant minimal 3 karakter"
                    )
                }
                return@launch
            }

            try {
                // Get FCM token
                val fcmToken = FirebaseMessaging.getInstance().token.await()

                // Generate merchant ID from name (sanitized)
                val merchantId = generateMerchantId(merchantName)

                // Register to backend
                merchantRepository.registerMerchant(
                    merchantId = merchantId,
                    merchantName = merchantName,
                    fcmToken = fcmToken,
                    phoneNumber = phoneNumber.ifEmpty { null }
                ).onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Gagal mendaftar"
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun generateMerchantId(name: String): String {
        // Sanitize name: remove special chars, take first 3 words, max 20 chars
        val sanitized = name
            .uppercase()
            .replace(Regex("[^A-Z0-9\\s]"), "")
            .split("\\s+".toRegex())
            .take(3)
            .joinToString("")
            .take(20)

        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        return "MERCHANT-${sanitized}-${timestamp}"
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
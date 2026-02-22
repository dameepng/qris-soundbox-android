package com.example.qris_soundbox.ui.qris

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.qris_soundbox.data.local.entity.QRISHistory
import com.example.qris_soundbox.data.repository.MerchantRepository
import com.example.qris_soundbox.data.repository.QRISRepository
import com.example.qris_soundbox.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─── UI State ─────────────────────────────────────────────

sealed class QRISUiState {
    object Idle : QRISUiState()
    object Loading : QRISUiState()
    data class QRISReady(val qrisData: QRISHistory) : QRISUiState()
    data class Error(val message: String) : QRISUiState()
    object Expired : QRISUiState()
    object Paid : QRISUiState()
}

class QRISViewModel(application: Application) : AndroidViewModel(application) {

    private val qrisRepository = QRISRepository(application)
    private val merchantRepository = MerchantRepository(application)

    // ─── UI State ──────────────────────────────────────────
    private val _uiState = MutableStateFlow<QRISUiState>(QRISUiState.Idle)
    val uiState: StateFlow<QRISUiState> = _uiState.asStateFlow()

    // ─── Countdown Timer ───────────────────────────────────
    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private var countdownJob: Job? = null

    // ─── Generate QRIS ─────────────────────────────────────

    fun generateQRIS(amount: Int) {
        viewModelScope.launch {
            _uiState.value = QRISUiState.Loading

            // Validate
            if (amount < Constants.QRIS_MIN_AMOUNT) {
                _uiState.value = QRISUiState.Error(
                    "Minimal pembayaran Rp ${
                        String.format("%,d", Constants.QRIS_MIN_AMOUNT)
                            .replace(',', '.')
                    }"
                )
                return@launch
            }

            // Get credentials
            val merchantId = merchantRepository.getMerchantId()
            val apiKey = merchantRepository.getApiKey()

            if (merchantId == null || apiKey == null) {
                _uiState.value = QRISUiState.Error(
                    "Merchant belum terdaftar. Silakan setup merchant terlebih dahulu."
                )
                return@launch
            }

            // Generate QRIS
            qrisRepository.generateDynamicQRIS(
                apiKey = apiKey,
                merchantId = merchantId,
                amount = amount
            ).onSuccess { qrisData ->
                _uiState.value = QRISUiState.QRISReady(qrisData)
                startCountdown(qrisData.expiresAt)

            }.onFailure { error ->
                _uiState.value = QRISUiState.Error(
                    error.message ?: "Gagal generate QR Code"
                )
            }
        }
    }

    // ─── Cancel QRIS ───────────────────────────────────────

    fun cancelQRIS() {
        val currentState = _uiState.value
        if (currentState !is QRISUiState.QRISReady) return

        viewModelScope.launch {
            countdownJob?.cancel()

            val apiKey = merchantRepository.getApiKey() ?: return@launch

            qrisRepository.cancelQRIS(
                apiKey = apiKey,
                orderId = currentState.qrisData.orderId
            )

            _uiState.value = QRISUiState.Idle
            _remainingSeconds.value = 0
        }
    }

    // ─── Reset ─────────────────────────────────────────────

    fun resetState() {
        countdownJob?.cancel()
        _uiState.value = QRISUiState.Idle
        _remainingSeconds.value = 0
    }

    // ─── Countdown ─────────────────────────────────────────

    private fun startCountdown(expiresAt: Long) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val remaining = (expiresAt - now) / 1000

                if (remaining <= 0) {
                    _remainingSeconds.value = 0
                    _uiState.value = QRISUiState.Expired
                    break
                }

                _remainingSeconds.value = remaining
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
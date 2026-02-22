package com.example.qris_soundbox.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.qris_soundbox.data.local.entity.MerchantSettings
import com.example.qris_soundbox.data.local.entity.PaymentTransaction
import com.example.qris_soundbox.data.repository.MerchantRepository
import com.example.qris_soundbox.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = false,
    val merchantSettings: MerchantSettings? = null,
    val todayTransactions: List<PaymentTransaction> = emptyList(),  // ← Update type
    val dailyTotal: Int = 0,
    val dailyCount: Int = 0,
    val isRegistered: Boolean = false,
    val error: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionRepository = TransactionRepository(application)
    private val merchantRepository = MerchantRepository(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {

            val isRegistered = merchantRepository.isRegistered()
            _uiState.update { it.copy(isRegistered = isRegistered) }

            launch {
                merchantRepository.getMerchantSettings()
                    .collect { settings ->
                        _uiState.update { it.copy(merchantSettings = settings) }
                    }
            }

            launch {
                transactionRepository.getTodayTransactions()
                    .collect { transactions ->
                        _uiState.update {
                            it.copy(todayTransactions = transactions)
                        }
                    }
            }

            launch {
                transactionRepository.getDailyTotal()
                    .collect { total ->
                        _uiState.update {
                            it.copy(dailyTotal = total ?: 0)
                        }
                    }
            }

            launch {
                transactionRepository.getDailyCount()
                    .collect { count ->
                        _uiState.update {
                            it.copy(dailyCount = count ?: 0)
                        }
                    }
            }
        }
    }

    fun refresh() {
        loadData()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
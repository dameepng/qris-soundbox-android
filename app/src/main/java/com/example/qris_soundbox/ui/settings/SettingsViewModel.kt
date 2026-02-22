package com.example.qris_soundbox.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.qris_soundbox.data.local.entity.MerchantSettings
import com.example.qris_soundbox.data.repository.MerchantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = false,
    val merchantSettings: MerchantSettings? = null,
    val successMessage: String? = null,
    val error: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val merchantRepository = MerchantRepository(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            merchantRepository.getMerchantSettings()
                .collect { settings ->
                    _uiState.update { it.copy(merchantSettings = settings) }
                }
        }
    }

    fun updateTTSEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val merchantId = merchantRepository.getMerchantId() ?: return@launch
            merchantRepository.updateTTSEnabled(merchantId, enabled)
            _uiState.update {
                it.copy(successMessage = if (enabled) "Suara diaktifkan" else "Suara dimatikan")
            }
        }
    }

    fun updateTTSVolume(volume: Float) {
        viewModelScope.launch {
            val merchantId = merchantRepository.getMerchantId() ?: return@launch
            merchantRepository.updateTTSVolume(merchantId, volume)
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }
}
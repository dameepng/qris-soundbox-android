package com.example.qris_soundbox.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qris_soundbox.ui.theme.*

class SettingsActivity : ComponentActivity() {

    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        setContent {
            SoundboxQRISTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                SettingsScreen(
                    uiState = uiState,
                    onNavigateBack = { finish() },
                    onTTSToggle = { enabled ->
                        viewModel.updateTTSEnabled(enabled)
                    },
                    onVolumeChange = { volume ->
                        viewModel.updateTTSVolume(volume)
                    },
                    onClearMessages = {
                        viewModel.clearMessages()
                    }
                )
            }
        }
    }
}

// ─── Settings Screen ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigateBack: () -> Unit,
    onTTSToggle: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onClearMessages: () -> Unit
) {
    // Show snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessages()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pengaturan",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ─── Merchant Info ────────────────────────────
            item {
                SettingsSectionHeader(title = "Informasi Merchant")
            }

            item {
                MerchantInfoCard(
                    merchantId = uiState.merchantSettings?.merchantId ?: "-",
                    merchantName = uiState.merchantSettings?.merchantName ?: "-"
                )
            }

            // ─── Audio Settings ───────────────────────────
            item {
                SettingsSectionHeader(title = "Pengaturan Suara")
            }

            item {
                AudioSettingsCard(
                    isTTSEnabled = uiState.merchantSettings?.ttsEnabled ?: true,
                    ttsVolume = uiState.merchantSettings?.ttsVolume ?: 1.0f,
                    onTTSToggle = onTTSToggle,
                    onVolumeChange = onVolumeChange
                )
            }

            // ─── Notification Settings ────────────────────
            item {
                SettingsSectionHeader(title = "Notifikasi")
            }

            item {
                NotificationSettingsCard()
            }

            // ─── App Info ─────────────────────────────────
            item {
                SettingsSectionHeader(title = "Tentang Aplikasi")
            }

            item {
                AppInfoCard()
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ─── Section Header ───────────────────────────────────────────

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = PrimaryGreen,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

// ─── Merchant Info Card ───────────────────────────────────────

@Composable
fun MerchantInfoCard(
    merchantId: String,
    merchantName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            SettingsRowItem(
                icon = Icons.Default.Store,
                title = "Nama Merchant",
                subtitle = merchantName
            )
            SettingsDivider()
            SettingsRowItem(
                icon = Icons.Default.Badge,
                title = "Merchant ID",
                subtitle = merchantId
            )
        }
    }
}

// ─── Audio Settings Card ──────────────────────────────────────

@Composable
fun AudioSettingsCard(
    isTTSEnabled: Boolean,
    ttsVolume: Float,
    onTTSToggle: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {

            // TTS Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Pengumuman Suara",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isTTSEnabled) "Aktif" else "Nonaktif",
                            fontSize = 12.sp,
                            color = if (isTTSEnabled) SuccessGreen else TextSecondary
                        )
                    }
                }

                Switch(
                    checked = isTTSEnabled,
                    onCheckedChange = onTTSToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryGreen
                    )
                )
            }

            // Volume Slider (only show if TTS enabled)
            if (isTTSEnabled) {
                SettingsDivider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Volume Suara",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "${(ttsVolume * 100).toInt()}%",
                            fontSize = 13.sp,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = ttsVolume,
                        onValueChange = onVolumeChange,
                        valueRange = 0.3f..1.0f,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryGreen,
                            activeTrackColor = PrimaryGreen,
                            inactiveTrackColor = PrimaryGreen.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }
}

// ─── Notification Settings Card ───────────────────────────────

@Composable
fun NotificationSettingsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            SettingsRowItem(
                icon = Icons.Default.Notifications,
                title = "Notifikasi Pembayaran",
                subtitle = "Aktif via Firebase FCM"
            )
            SettingsDivider()
            SettingsRowItem(
                icon = Icons.Default.BatteryFull,
                title = "Battery Optimization",
                subtitle = "Pastikan dinonaktifkan untuk performa terbaik"
            )
        }
    }
}

// ─── App Info Card ────────────────────────────────────────────

@Composable
fun AppInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            SettingsRowItem(
                icon = Icons.Default.Info,
                title = "Versi Aplikasi",
                subtitle = "1.0.0 (Build 1)"
            )
            SettingsDivider()
            SettingsRowItem(
                icon = Icons.Default.Security,
                title = "Payment Gateway",
                subtitle = "Midtrans QRIS"
            )
        }
    }
}

// ─── Reusable Components ──────────────────────────────────────

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun SettingsDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = DividerColor
    )
}
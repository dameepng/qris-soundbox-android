package com.example.qris_soundbox.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qris_soundbox.data.local.entity.PaymentTransaction
import com.example.qris_soundbox.ui.qris.QRISGeneratorActivity
import com.example.qris_soundbox.ui.settings.SettingsActivity
import com.example.qris_soundbox.ui.theme.BackgroundLight
import com.example.qris_soundbox.ui.theme.PrimaryGreen
import com.example.qris_soundbox.ui.theme.SoundboxQRISTheme
import com.example.qris_soundbox.ui.theme.SuccessGreen
import com.example.qris_soundbox.ui.theme.SurfaceWhite
import com.example.qris_soundbox.ui.theme.TextPrimary
import com.example.qris_soundbox.ui.theme.TextSecondary
import com.example.qris_soundbox.utils.toDateString
import com.example.qris_soundbox.utils.toRupiah

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Show dialog explaining why notification is needed
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Request notification permission
        requestNotificationPermission()

        setContent {
            SoundboxQRISTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                MainScreen(
                    uiState = uiState,
                    onNavigateToQRIS = {
                        startActivity(
                            Intent(this, QRISGeneratorActivity::class.java)
                        )
                    },
                    onNavigateToSettings = {
                        startActivity(
                            Intent(this, SettingsActivity::class.java)
                        )
                    },
                    onRefresh = { viewModel.refresh() }
                )
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> { /* Already granted */ }

                else -> notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }
}

// ─── Main Screen Composable ───────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    onNavigateToQRIS: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Soundbox QRIS",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToQRIS,
                icon = {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Generate QR"
                    )
                },
                text = { Text("Buat QR Code") },
                containerColor = PrimaryGreen,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ─── Daily Summary Card ──────────────────────
            item {
                DailySummaryCard(
                    totalAmount = uiState.dailyTotal,
                    totalCount = uiState.dailyCount,
                    merchantName = uiState.merchantSettings?.merchantName ?: "Merchant"
                )
            }

            // ─── Transaction Header ──────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaksi Hari Ini",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    TextButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refresh")
                    }
                }
            }

            // ─── Empty State ─────────────────────────────
            if (uiState.todayTransactions.isEmpty()) {
                item {
                    EmptyTransactionCard()
                }
            }

            // ─── Transaction List ────────────────────────
            items(
                items = uiState.todayTransactions,
                key = { it.transactionId }
            ) { transaction ->
                TransactionItem(transaction = transaction)
            }

            // Bottom spacing for FAB
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

// ─── Daily Summary Card ───────────────────────────────────────

@Composable
fun DailySummaryCard(
    totalAmount: Int,
    totalCount: Int,
    merchantName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryGreen
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = merchantName,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Total Hari Ini",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )

            Text(
                text = totalAmount.toRupiah(),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color.White.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryStatItem(
                    label = "Transaksi",
                    value = "$totalCount kali"
                )
                SummaryStatItem(
                    label = "Status",
                    value = "🟢 Aktif"
                )
            }
        }
    }
}

@Composable
fun SummaryStatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Transaction Item ─────────────────────────────────────────

@Composable
fun TransactionItem(transaction: PaymentTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Status icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SuccessGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Pembayaran QRIS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = transaction.timestamp.toDateString(),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Amount
            Text(
                text = transaction.amount.toRupiah(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SuccessGreen
            )
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────

@Composable
fun EmptyTransactionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "💳", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Belum ada transaksi hari ini",
                fontSize = 14.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap tombol 'Buat QR Code' untuk mulai",
                fontSize = 12.sp,
                color = TextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}
package com.example.qris_soundbox.ui.qris

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qris_soundbox.ui.theme.BackgroundLight
import com.example.qris_soundbox.ui.theme.ErrorRed
import com.example.qris_soundbox.ui.theme.PrimaryGreen
import com.example.qris_soundbox.ui.theme.SoundboxQRISTheme
import com.example.qris_soundbox.ui.theme.SuccessGreen
import com.example.qris_soundbox.ui.theme.SurfaceWhite
import com.example.qris_soundbox.ui.theme.TextPrimary
import com.example.qris_soundbox.ui.theme.TextSecondary
import com.example.qris_soundbox.utils.toRupiah
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class QRISGeneratorActivity : ComponentActivity() {

    private lateinit var viewModel: QRISViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[QRISViewModel::class.java]

        setContent {
            SoundboxQRISTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()

                QRISGeneratorScreen(
                    uiState = uiState,
                    remainingSeconds = remainingSeconds,
                    onGenerateQRIS = { amount -> viewModel.generateQRIS(amount) },
                    onCancelQRIS = { viewModel.cancelQRIS() },
                    onResetState = { viewModel.resetState() },
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

// ─── QRIS Generator Screen ────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRISGeneratorScreen(
    uiState: QRISUiState,
    remainingSeconds: Long,
    onGenerateQRIS: (Int) -> Unit,
    onCancelQRIS: () -> Unit,
    onResetState: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Observe state changes to show dialog
    androidx.compose.runtime.LaunchedEffect(uiState) {
        if (uiState is QRISUiState.Paid) {
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Pembayaran Selesai", fontWeight = FontWeight.Bold) },
            text = { Text("Pembayaran Anda telah berhasil diproses.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onResetState()
                        amountInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Buat QR Baru")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSuccessDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            containerColor = SurfaceWhite,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat QR Code", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState) {
                // ─── Idle, Error & Paid State ───────────────────
                is QRISUiState.Idle, is QRISUiState.Error, is QRISUiState.Paid -> {
                    InputSection(
                        amountInput = amountInput,
                        onAmountChange = { amountInput = it },
                        onGenerate = {
                            val amount = amountInput
                                .replace(".", "")
                                .toIntOrNull() ?: 0
                            onGenerateQRIS(amount)
                        },
                        error = if (uiState is QRISUiState.Error) uiState.message else null
                    )
                }

                // ─── Loading State ────────────────────────
                is QRISUiState.Loading -> {
                    Spacer(modifier = Modifier.height(80.dp))
                    CircularProgressIndicator(
                        color = PrimaryGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Membuat QR Code...",
                        color = TextSecondary
                    )
                }

                // ─── QRIS Ready State ─────────────────────
                is QRISUiState.QRISReady -> {
                    QRISReadySection(
                        qrisString = uiState.qrisData.qrisString,
                        amount = uiState.qrisData.amount,
                        remainingSeconds = remainingSeconds,
                        onCancel = onCancelQRIS,
                        onNewQR = {
                            onResetState()
                            amountInput = ""
                        }
                    )
                }

                // ─── Expired State ────────────────────────
                is QRISUiState.Expired -> {
                    ExpiredSection(
                        onNewQR = {
                            onResetState()
                            amountInput = ""
                        }
                    )
                }

                }
            }
        }
    }

// ─── Input Section ────────────────────────────────────────────

@Composable
fun InputSection(
    amountInput: String,
    onAmountChange: (String) -> Unit,
    onGenerate: () -> Unit,
    error: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(
                text = "Masukkan Nominal",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Amount Input
            OutlinedTextField(
                value = amountInput,
                onValueChange = { value ->
                    val cleaned = value.filter { it.isDigit() }
                    onAmountChange(cleaned)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nominal (Rp)") },
                prefix = { Text("Rp ") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                isError = error != null,
                supportingText = error?.let { { Text(it, color = ErrorRed) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    focusedLabelColor = PrimaryGreen
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Amount Buttons
            Text(
                text = "Nominal cepat:",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            val quickAmounts = listOf(5_000, 10_000, 25_000, 50_000, 100_000)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickAmounts.forEach { amount ->
                    FilterChip(
                        selected = amountInput == amount.toString(),
                        onClick = { onAmountChange(amount.toString()) },
                        label = {
                            Text(
                                text = when {
                                    amount >= 1_000 -> "${amount / 1_000}K"
                                    else -> amount.toString()
                                },
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryGreen,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Generate Button
            Button(
                onClick = onGenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen
                ),
                enabled = amountInput.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buat QR Code",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── QRIS Ready Section ───────────────────────────────────────

@Composable
fun QRISReadySection(
    qrisString: String,
    amount: Int,
    remainingSeconds: Long,
    onCancel: () -> Unit,
    onNewQR: () -> Unit
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val isWarning = remainingSeconds < 60

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // Amount Badge
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryGreen)
        ) {
            Text(
                text = amount.toRupiah(),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // QR Code
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // QR Code Image
                val qrBitmap = remember(qrisString) {
                    generateQRCode(qrisString, 600, 600)
                }

                qrBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QRIS Code",
                        modifier = Modifier.size(250.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Timer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (isWarning) ErrorRed else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Berlaku: %02d:%02d".format(minutes, seconds),
                        fontSize = 13.sp,
                        color = if (isWarning) ErrorRed else TextSecondary,
                        fontWeight = if (isWarning) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tunjukkan ke customer",
                    fontSize = 12.sp,
                    color = TextSecondary.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ErrorRed
                )
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Batal")
            }

            Button(
                onClick = onNewQR,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("QR Baru")
            }
        }
    }
}

// ─── Expired Section ──────────────────────────────────────────

@Composable
fun ExpiredSection(onNewQR: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Text(text = "⏱️", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "QR Code Expired",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ErrorRed
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "QR Code sudah tidak berlaku. Silakan buat yang baru.",
            color = TextSecondary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNewQR,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Buat QR Baru")
        }
    }
}

// ─── QR Code Generator Helper ─────────────────────────────────

fun generateQRCode(content: String, width: Int, height: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
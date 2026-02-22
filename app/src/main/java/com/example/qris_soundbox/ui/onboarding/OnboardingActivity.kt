package com.example.qris_soundbox.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qris_soundbox.ui.main.MainActivity
import com.example.qris_soundbox.ui.theme.BackgroundLight
import com.example.qris_soundbox.ui.theme.PrimaryGreen
import com.example.qris_soundbox.ui.theme.SoundboxQRISTheme
import com.example.qris_soundbox.ui.theme.SurfaceWhite
import com.example.qris_soundbox.ui.theme.TextPrimary
import com.example.qris_soundbox.ui.theme.TextSecondary

class OnboardingActivity : ComponentActivity() {

    private lateinit var viewModel: OnboardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[OnboardingViewModel::class.java]

        setContent {
            SoundboxQRISTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // Navigate to MainActivity on success
                LaunchedEffect(uiState.isSuccess) {
                    if (uiState.isSuccess) {
                        startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                        finish()
                    }
                }

                OnboardingScreen(
                    uiState = uiState,
                    onMerchantNameChange = { viewModel.updateMerchantName(it) },
                    onPhoneNumberChange = { viewModel.updatePhoneNumber(it) },
                    onRegisterClick = { viewModel.registerMerchant() },
                    onErrorDismiss = { viewModel.clearError() }
                )
            }
        }
    }
}

// ─── Onboarding Screen ────────────────────────────────────

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onMerchantNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            onErrorDismiss()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Logo/Icon
                Text(
                    text = "🔊",
                    fontSize = 80.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Title
                Text(
                    text = "Soundbox QRIS",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Notifikasi pembayaran instan",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Form Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceWhite
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {

                        Text(
                            text = "Daftar Merchant",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Merchant Name Input
                        OutlinedTextField(
                            value = uiState.merchantName,
                            onValueChange = onMerchantNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Nama Merchant") },
                            placeholder = { Text("Contoh: Warung Pak Budi") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null
                                )
                            },
                            singleLine = true,
                            enabled = !uiState.isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                focusedLabelColor = PrimaryGreen,
                                focusedLeadingIconColor = PrimaryGreen
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Phone Number Input (Optional)
                        OutlinedTextField(
                            value = uiState.phoneNumber,
                            onValueChange = onPhoneNumberChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("No. Telepon (Opsional)") },
                            placeholder = { Text("08123456789") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null
                                )
                            },
                            singleLine = true,
                            enabled = !uiState.isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGreen,
                                focusedLabelColor = PrimaryGreen,
                                focusedLeadingIconColor = PrimaryGreen
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Register Button
                        Button(
                            onClick = onRegisterClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGreen
                            ),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mendaftar...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Daftar Sekarang",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info Text
                Text(
                    text = "Dengan mendaftar, Anda akan menerima notifikasi pembayaran real-time via Firebase",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}
package com.example.qris_soundbox.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qris_soundbox.data.repository.MerchantRepository
import com.example.qris_soundbox.ui.main.MainActivity
import com.example.qris_soundbox.ui.onboarding.OnboardingActivity
import com.example.qris_soundbox.ui.theme.PrimaryGreen
import com.example.qris_soundbox.ui.theme.SoundboxQRISTheme
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {

    private lateinit var merchantRepository: MerchantRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        merchantRepository = MerchantRepository(this)

        setContent {
            SoundboxQRISTheme {
                SplashScreen(
                    onNavigateToNext = {
                        // Check if merchant already registered
                        val isRegistered = merchantRepository.isRegistered()

                        val intent = if (isRegistered) {
                            Intent(this, MainActivity::class.java)
                        } else {
                            Intent(this, OnboardingActivity::class.java)
                        }

                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SplashScreen(onNavigateToNext: () -> Unit) {

    // Animation
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )

        delay(2000)
        onNavigateToNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🔊",
                fontSize = 80.sp,
                modifier = Modifier.scale(scale.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Soundbox QRIS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.scale(scale.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Notifikasi pembayaran instan",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .graphicsLayer(alpha = alpha.value)  // ← FIX
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer(alpha = alpha.value)  // ← FIX
            )
        }
    }
}
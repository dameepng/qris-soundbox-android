package com.example.qris_soundbox.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.qris_soundbox.ui.main.MainScreen
import com.example.qris_soundbox.ui.main.MainViewModel
import com.example.qris_soundbox.ui.qris.QRISGeneratorScreen
import com.example.qris_soundbox.ui.qris.QRISViewModel
import com.example.qris_soundbox.ui.settings.SettingsScreen
import com.example.qris_soundbox.ui.settings.SettingsViewModel
import com.example.qris_soundbox.ui.splash.SplashScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash.route
    ) {
        composable(NavRoutes.Splash.route) {
            SplashScreen(
                onNavigateToNext = {
                    navController.navigate(NavRoutes.Main.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Main.route) {
            val viewModel: MainViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MainScreen(
                uiState = uiState,
                onNavigateToQRIS = {
                    navController.navigate(NavRoutes.QRISGenerator.route)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.Settings.route)
                },
                onRefresh = { viewModel.refresh() }
            )
        }

        composable(NavRoutes.QRISGenerator.route) {
            val viewModel: QRISViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()

            QRISGeneratorScreen(
                uiState = uiState,
                remainingSeconds = remainingSeconds,
                onGenerateQRIS = { amount -> viewModel.generateQRIS(amount) },
                onCancelQRIS = { viewModel.cancelQRIS() },
                onResetState = { viewModel.resetState() },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.Settings.route) {
            val viewModel: SettingsViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            SettingsScreen(
                uiState = uiState,
                onTTSToggle = { viewModel.updateTTSEnabled(it) },
                onVolumeChange = { viewModel.updateTTSVolume(it) },
                onClearMessages = { viewModel.clearMessages() },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
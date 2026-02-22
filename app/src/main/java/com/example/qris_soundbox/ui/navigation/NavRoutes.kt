package com.example.qris_soundbox.ui.navigation

sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("splash")
    object Main : NavRoutes("main")
    object QRISGenerator : NavRoutes("qris_generator")
    object Settings : NavRoutes("settings")
    object TransactionHistory : NavRoutes("transaction_history")
}
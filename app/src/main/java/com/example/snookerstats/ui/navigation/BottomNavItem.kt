package com.example.snookerstats.ui.navigation

import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Dashboard : BottomNavItem("dashboard", Filled.Home, "Dashboard")
    object Play : BottomNavItem("play", Filled.Home, "Graj") // Zmieniono na Filled.Home jako tymczasowy zamiennik
    object Community : BottomNavItem("community", Filled.Star, "Społeczność") // Zmieniono na Filled.Star jako tymczasowy zamiennik
    object Tournaments : BottomNavItem("tournaments", Filled.Star, "Turnieje")
    object Profile : BottomNavItem("profile", Filled.AccountCircle, "Profil")
}
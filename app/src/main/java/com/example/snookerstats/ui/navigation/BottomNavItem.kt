package com.example.snookerstats.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Główna")
    object Play : BottomNavItem("play", Icons.Default.Home, "Graj") // Tymczasowo zmieniono na Home
    object MatchHistory : BottomNavItem("match_history", Icons.Default.Home, "Mecze") // Zmieniono z "Historia Meczów"
    object Stats : BottomNavItem("stats", Icons.Default.Star, "Statystyki") // Tymczasowo zmieniono na Star
    object Community : BottomNavItem("community", Icons.Default.Star, "Ludzie") // Zmieniono z "Społeczność" na "Ludzie"
    object Profile : BottomNavItem("profile", Icons.Default.AccountCircle, "Profil") // Dodana definicja profilu
}

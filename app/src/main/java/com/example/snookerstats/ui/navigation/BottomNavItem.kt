package com.example.snookerstats.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Główna")
    object Play : BottomNavItem("play", Icons.Default.PlayArrow, "Graj")
    object MatchHistory : BottomNavItem("match_history", Icons.Default.History, "Historia")
    object Stats : BottomNavItem("stats", Icons.Default.BarChart, "Statystyki")
    object Community : BottomNavItem("community", Icons.Default.People, "Ludzie")
    object Profile : BottomNavItem("profile", Icons.Default.AccountCircle, "Profil")
}

package com.example.snookerstats.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.snookerstats.ui.auth.AuthViewModel
import com.example.snookerstats.ui.auth.NavigationEvent
import com.example.snookerstats.ui.navigation.BottomNavItem
import com.example.snookerstats.ui.screens.CommunityScreen
import com.example.snookerstats.ui.screens.HomeScreen
import com.example.snookerstats.ui.screens.MatchHistoryScreen
import com.example.snookerstats.ui.screens.PlayScreen
import com.example.snookerstats.ui.screens.ProfileScreen
import com.example.snookerstats.ui.screens.StatsScreen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val internalNavController = rememberNavController()

    LaunchedEffect(Unit) {
        authViewModel.navigationEvent.collectLatest { event ->
            if (event is NavigationEvent.NavigateToLogin) {
                navController.navigate("login") {
                    popUpTo("main") { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Snooker Stats") },
                actions = {
                    IconButton(onClick = { internalNavController.navigate(BottomNavItem.Profile.route) }) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Profil"
                        )
                    }
                    IconButton(onClick = { 
                        authViewModel.signOut()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Wyloguj"
                        )
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController = internalNavController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavigationGraph(navController = internalNavController)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Play,
        BottomNavItem.MatchHistory,
        BottomNavItem.Stats,
        BottomNavItem.Community
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) { HomeScreen() }
        composable(BottomNavItem.Play.route) { PlayScreen() }
        composable(BottomNavItem.MatchHistory.route) { MatchHistoryScreen() }
        composable(BottomNavItem.Stats.route) { StatsScreen() }
        composable(BottomNavItem.Community.route) { CommunityScreen() }
        composable(BottomNavItem.Profile.route) { ProfileScreen() }
    }
}

package com.example.snookerstats.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.ui.chats.ChatListScreen
import com.example.snookerstats.ui.navigation.BottomNavItem
import com.example.snookerstats.ui.screens.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    snackbarManager: SnackbarManager,
    viewModel: MainViewModel = hiltViewModel(),
    authRepository: AuthRepository = hiltViewModel()
) {
    val internalNavController = rememberNavController()
    val username by viewModel.username.collectAsState()
    val snackbarHostState = snackbarManager.snackbarHostState
    val scope = rememberCoroutineScope()

    val navBackStackEntry by internalNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Play,
        BottomNavItem.MatchHistory,
        BottomNavItem.Stats,
        BottomNavItem.Community
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = username) },
                actions = {
                    IconButton(onClick = { navController.navigate("chat_list") }) {
                        Icon(Icons.Default.Forum, contentDescription = "Czat")
                    }
                    IconButton(onClick = { internalNavController.navigate("profile/${authRepository.currentUser?.uid}") }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profil")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            authRepository.logout()
                            navController.navigate("login") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Wyloguj")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            internalNavController.navigate(item.route) {
                                popUpTo(internalNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(internalNavController, startDestination = BottomNavItem.Home.route) {
                composable(BottomNavItem.Home.route) { HomeScreen() }
                composable(BottomNavItem.Play.route) { PlayScreen() }
                composable(BottomNavItem.MatchHistory.route) { MatchHistoryScreen() }
                composable(BottomNavItem.Stats.route) { StatsScreen() }
                composable(BottomNavItem.Community.route) { CommunityScreen(navController = internalNavController) }
                composable("chat_list") { ChatListScreen(navController = navController) }
                composable(
                    route = "profile/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.StringType; nullable = true })
                ) {
                    UserProfileScreen(navController = internalNavController)
                }
            }
        }
    }
}

package com.example.snookerstats.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Forum
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.example.snookerstats.ui.chat.ChatListScreen

import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val internalNavController = rememberNavController()
    val username by mainViewModel.username.collectAsState()

    // Tymczasowy stan dla nieprzeczytanych wiadomości do celów demonstracyjnych
    val unreadMessagesCount by remember { mutableStateOf(3) }

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
                title = { Text(username) },
                actions = {
                    IconButton(
                        onClick = {
                            internalNavController.navigate("chat_list") {
                                popUpTo(internalNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        // Usunięto: modifier = Modifier.padding(end = 4.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadMessagesCount > 0) {
                                    Badge(
                                        modifier = Modifier.offset(x = (-12).dp, y = (-4).dp) // Dostosowano przesunięcie badge'a bardziej w lewo
                                    ) {
                                        Text(text = unreadMessagesCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Forum,
                                contentDescription = "Wiadomości"
                            )
                        }
                    }
                    IconButton(onClick = {
                        internalNavController.navigate(BottomNavItem.Profile.route) {
                            popUpTo(internalNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }) {
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
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
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

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) { HomeScreen() }
        composable(BottomNavItem.Play.route) { PlayScreen() }
        composable(BottomNavItem.MatchHistory.route) { MatchHistoryScreen() }
        composable(BottomNavItem.Stats.route) { StatsScreen() }
        composable(BottomNavItem.Community.route) { CommunityScreen() }
        composable(BottomNavItem.Profile.route) { ProfileScreen() }
        composable("chat_list") { ChatListScreen() }
    }
}

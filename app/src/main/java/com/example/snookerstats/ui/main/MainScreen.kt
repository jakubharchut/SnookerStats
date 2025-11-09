package com.example.snookerstats.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.snookerstats.ui.auth.AuthViewModel
import com.example.snookerstats.ui.auth.NavigationEvent
import com.example.snookerstats.ui.chat.ChatListScreen
import com.example.snookerstats.ui.navigation.BottomNavItem
import com.example.snookerstats.ui.notifications.NotificationViewModel
import com.example.snookerstats.ui.screens.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    snackbarManager: SnackbarManager,
    authViewModel: AuthViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val internalNavController = rememberNavController()
    val username by mainViewModel.username.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val unreadNotificationsCount by notificationViewModel.unreadNotificationCount.collectAsState()

    // Efekt do obsługi nawigacji (w tym wylogowania)
    LaunchedEffect(Unit) {
        authViewModel.navigationEvent.collectLatest { event ->
            if (event is NavigationEvent.NavigateToLogin) {
                navController.navigate("login") {
                    popUpTo("main") { inclusive = true }
                }
            }
        }
    }

    // Prośba o uprawnienia do powiadomień na Android 13+
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // Uprawnienie przyznane
                snackbarManager.showMessage("Zgoda na powiadomienia udzielona.")
            } else {
                // Uprawnienie odrzucone
                snackbarManager.showMessage("Powiadomienia mogą nie działać bez zgody.")
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU to Android 13
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val snackbarMessage by snackbarManager.messages.collectAsState()
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            snackbarManager.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text(
                            text = data.visuals.message,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(username) },
                actions = {
                    IconButton(onClick = {
                        internalNavController.navigate("chat_list") {
                            popUpTo(internalNavController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }) {
                        Icon(imageVector = Icons.Filled.Forum, contentDescription = "Wiadomości")
                    }
                    IconButton(onClick = {
                        internalNavController.navigate("notifications") {
                            popUpTo(internalNavController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(
                                        modifier = Modifier.offset(x = (-12).dp, y = (-4).dp)
                                    ) {
                                        Text(text = unreadNotificationsCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Powiadomienia")
                        }
                    }
                    IconButton(onClick = {
                        internalNavController.navigate("profile") {
                            popUpTo(internalNavController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }) {
                        Icon(imageVector = Icons.Filled.AccountCircle, contentDescription = "Profil")
                    }
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Wyloguj")
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
                selected = currentDestination?.hierarchy?.any { it.route?.startsWith(item.route) == true } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id)
                        launchSingleTop = true
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
        composable(
            route = "community?initialTabIndex={initialTabIndex}",
            arguments = listOf(navArgument("initialTabIndex") {
                type = NavType.IntType
                defaultValue = 0
            })
        ) { backStackEntry ->
            val initialTabIndex = backStackEntry.arguments?.getInt("initialTabIndex") ?: 0
            CommunityScreen(navController = navController, initialTabIndex = initialTabIndex)
        }
        composable("chat_list") { ChatListScreen() }
        composable("notifications") { NotificationsScreen(navController = navController) }
        composable(
            route = "profile?userId={userId}",
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
                nullable = true
            })
        ) {
            UserProfileScreen()
        }
        composable(
            route = "user_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            navController.navigate("profile?userId=${backStackEntry.arguments?.getString("userId")}")
        }
    }
}

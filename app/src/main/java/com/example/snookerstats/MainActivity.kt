package com.example.snookerstats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.ui.auth.LoginScreen
import com.example.snookerstats.ui.auth.RegisterScreen
import com.example.snookerstats.ui.auth.RegistrationSuccessScreen
import com.example.snookerstats.ui.chats.ChatListScreen
import com.example.snookerstats.ui.chats.ConversationScreen
import com.example.snookerstats.ui.main.MainScreen
import com.example.snookerstats.ui.main.SnackbarManager
import com.example.snookerstats.ui.profile.SetupProfileScreen
import com.example.snookerstats.ui.theme.SnookerStatsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var snackbarManager: SnackbarManager

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnookerStatsTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login", modifier = Modifier.fillMaxSize()) {
                        composable("login") {
                            LoginScreen(navController = navController)
                        }
                        composable("register") {
                            RegisterScreen(navController = navController)
                        }
                        composable("registration_success") {
                            RegistrationSuccessScreen(navController = navController)
                        }
                        composable("setup_profile") {
                            SetupProfileScreen(navController = navController)
                        }
                        composable("main") {
                            MainScreen(navController = navController, snackbarManager = snackbarManager)
                        }
                        composable("chat_list") {
                            ChatListScreen(navController = navController)
                        }
                        composable(
                            route = "conversation/{chatId}/{otherUserName}",
                            arguments = listOf(
                                navArgument("chatId") { type = NavType.StringType },
                                navArgument("otherUserName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val chatId = backStackEntry.arguments?.getString("chatId")!!
                            val otherUserName = backStackEntry.arguments?.getString("otherUserName")!!
                            ConversationScreen(
                                navController = navController,
                                otherUserName = otherUserName,
                                authRepository = authRepository
                            )
                        }
                    }
                }
            }
        }
    }
}

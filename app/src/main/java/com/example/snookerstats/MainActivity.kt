package com.example.snookerstats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.snookerstats.ui.auth.LoginScreen
import com.example.snookerstats.ui.auth.RegisterScreen
import com.example.snookerstats.ui.auth.RegistrationSuccessScreen
import com.example.snookerstats.ui.main.MainScreen
import com.example.snookerstats.ui.theme.SnookerStatsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnookerStatsTheme {
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
                        composable("main") {
                            MainScreen(navController = navController) // Poprawka: przekazanie navController
                        }
                    }
                }
            }
        }
    }
}

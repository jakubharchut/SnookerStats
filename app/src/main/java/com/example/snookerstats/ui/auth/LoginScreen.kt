package com.example.snookerstats.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.snookerstats.ui.theme.SnookerStatsTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val credentialsState by viewModel.credentialsState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Wypełnij pola, jeśli dane są zapisane
    LaunchedEffect(credentialsState) {
        if (credentialsState.email.isNotEmpty()) {
            email = credentialsState.email
            password = credentialsState.password
            rememberMe = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateToMain -> {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                is NavigationEvent.NavigateToSetupProfile -> {
                    navController.navigate("setup_profile") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onKeyEvent {
                        if (it.key == Key.Tab) {
                            focusManager.moveFocus(FocusDirection.Down)
                            true
                        } else {
                            false
                        }
                    },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Hasło") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.loginUser(email, password, rememberMe)
                })
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it }
                )
                Text("Zapamiętaj mnie")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { 
                    focusManager.clearFocus()
                    viewModel.loginUser(email, password, rememberMe) 
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Zaloguj się")
                }
            }

            when (val state = authState) {
                is AuthState.Error -> Text(state.message, color = Color.Red)
                else -> {}
            }
        }

        TextButton(
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = { navController.navigate("register") }
        ) {
            Text("Nie masz konta? Zarejestruj się")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SnookerStatsTheme {
        LoginScreen(navController = rememberNavController())
    }
}

package com.example.snookerstats.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val credentialsState by viewModel.credentialsState.collectAsState()
    var email by remember { mutableStateOf(credentialsState.email) }
    var password by remember { mutableStateOf(credentialsState.password) }
    var rememberMe by remember { mutableStateOf(false) } // Dodano stan dla checkboxa

    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(credentialsState) {
        if (credentialsState.email.isNotEmpty()) {
            email = credentialsState.email
        }
        if (credentialsState.password.isNotEmpty()) {
            password = credentialsState.password
        }
        // Po załadowaniu danych, domyślnie zaznacz checkbox, jeśli są dane
        rememberMe = credentialsState.email.isNotEmpty() || credentialsState.password.isNotEmpty()
    }

    val loginAction = {
        focusManager.clearFocus()
        viewModel.loginUser(email, password, rememberMe) // Przekazanie stanu checkboxa
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            if (event is NavigationEvent.NavigateToMain) {
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
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
                keyboardActions = KeyboardActions(onDone = { loginAction() })
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it }
                )
                Text("Zapamiętaj mnie")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { loginAction() },
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
                is AuthState.Success -> Text(state.message, color = Color.Green)
                is AuthState.Error -> Text(state.message, color = Color.Red)
                else -> {} // Obsługa pozostałych stanów
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

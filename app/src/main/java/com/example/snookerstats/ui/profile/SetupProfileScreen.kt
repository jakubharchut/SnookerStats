package com.example.snookerstats.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.ui.theme.SnookerStatsTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SetupProfileScreen(
    navController: NavController,
    viewModel: SetupProfileViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var isPublicProfile by remember { mutableStateOf(true) }

    val setupState by viewModel.setupState.collectAsState()
    val usernameValidationState by viewModel.usernameValidationState.collectAsState()
    
    val isUsernameFormatValid = username.isNotBlank() && !username.contains(" ")
    val isButtonEnabled = isUsernameFormatValid &&
                          usernameValidationState.isAvailable == true &&
                          firstName.isNotBlank() &&
                          lastName.isNotBlank()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is ProfileNavigationEvent.NavigateToMain -> {
                    navController.navigate("main") {
                        popUpTo("setup_profile") { inclusive = true }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Uzupełnij swój profil",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Witaj w Snooker Stats! Aby w pełni korzystać z aplikacji, musisz jednorazowo uzupełnić swój profil.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                viewModel.onUsernameChange(it)
            },
            label = { Text("Nazwa użytkownika") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = (username.isNotEmpty() && !isUsernameFormatValid) || usernameValidationState.isAvailable == false,
            supportingText = {
                if (username.isNotEmpty()) {
                    when {
                        !isUsernameFormatValid -> Text("Nazwa użytkownika nie może zawierać spacji.")
                        usernameValidationState.isChecking -> Text("Sprawdzanie dostępności...")
                        usernameValidationState.isAvailable == false -> Text("Ta nazwa jest już zajęta.")
                        usernameValidationState.isAvailable == true -> Text("Nazwa dostępna!", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("Imię") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Nazwisko") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Profil publiczny", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = isPublicProfile,
                onCheckedChange = { isPublicProfile = it }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val user = User(
                    username = username.trim(),
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    publicProfile = isPublicProfile
                )
                viewModel.saveUserProfile(user)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = setupState !is SetupProfileState.Loading && isButtonEnabled
        ) {
            if (setupState is SetupProfileState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Zapisz i kontynuuj")
            }
        }
        
        if (setupState is SetupProfileState.Error) {
            val errorState = setupState as SetupProfileState.Error
            Text(
                text = errorState.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SetupProfileScreenPreview() {
    SnookerStatsTheme {
        SetupProfileScreen(navController = rememberNavController())
    }
}

package com.example.snookerstats.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // Dodano brakujący import
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.ui.theme.SnookerStatsTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SetupProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var isPublicProfile by remember { mutableStateOf(true) } // Domyślnie publiczny

    val profileState by viewModel.profileState.collectAsState()

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
        Spacer(modifier = Modifier.height(32.dp)) // Większa przestrzeń od góry
        Text(
            text = "Uzupełnij swój profil",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Witaj w Snooker Stats! Aby w pełni korzystać z aplikacji, musisz jednorazowo uzupełnić swój profil. Nazwa użytkownika jest obowiązkowa.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Dane obowiązkowe",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nazwa użytkownika") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Dane opcjonalne",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
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
            Text("Ukryj profil (domyślnie publiczny)", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = !isPublicProfile, // Odwrotna logika - checked oznacza UKRYTY
                onCheckedChange = { isPublicProfile = !it }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val user = User(
                    username = username.trim(),
                    firstName = firstName.trim().ifEmpty { null },
                    lastName = lastName.trim().ifEmpty { null },
                    isPublicProfile = isPublicProfile
                )
                viewModel.saveUserProfile(user)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = profileState !is ProfileState.Loading && username.isNotBlank()
        ) {
            if (profileState is ProfileState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Zapisz i kontynuuj")
            }
        }
        
        if (profileState is ProfileState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                (profileState as ProfileState.Error).message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
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

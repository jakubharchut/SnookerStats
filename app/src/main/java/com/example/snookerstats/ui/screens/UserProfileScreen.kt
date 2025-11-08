package com.example.snookerstats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.snookerstats.ui.profile.ProfileState
import com.example.snookerstats.ui.profile.ProfileViewModel

@Composable
fun UserProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.profileState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val profileState = state) {
            is ProfileState.Loading -> {
                CircularProgressIndicator()
            }
            is ProfileState.Error -> {
                Text(
                    text = "Błąd: ${profileState.message}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            is ProfileState.Success -> {
                if (profileState.canViewProfile) {
                    // Widok publiczny lub dla znajomego
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Profil użytkownika", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Nazwa: ${profileState.targetUser.username}")
                        Text(text = "Imię: ${profileState.targetUser.firstName ?: "Brak"}")
                        Text(text = "Nazwisko: ${profileState.targetUser.lastName ?: "Brak"}")
                        // Tutaj w przyszłości pojawią się statystyki
                    }
                } else {
                    // Widok prywatny
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Profil prywatny",
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Profil użytkownika ${profileState.targetUser.username} jest prywatny",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Dodaj go do znajomych, aby zobaczyć więcej.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { /* TODO: Handle send friend request */ }) {
                            Text("Dodaj do znajomych")
                        }
                    }
                }
            }
        }
    }
}

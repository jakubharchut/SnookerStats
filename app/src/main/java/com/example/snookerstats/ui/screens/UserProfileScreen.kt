package com.example.snookerstats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.snookerstats.ui.common.UserAvatar
import com.example.snookerstats.ui.profile.ProfileNavigationEvent
import com.example.snookerstats.ui.profile.ProfileState
import com.example.snookerstats.ui.profile.ProfileViewModel

@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            if (event is ProfileNavigationEvent.NavigateToChat) {
                // This navigation should be handled by the main NavController
                // We might need to pass it down or handle it via a callback
            }
        }
    }

    when (val state = profileState) {
        is ProfileState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ProfileState.Success -> {
            val canViewProfile = state.targetUser.publicProfile || state.relationshipStatus == RelationshipStatus.FRIENDS || state.relationshipStatus == RelationshipStatus.SELF
            if (canViewProfile) {
                PublicProfileContent(navController = navController, viewModel = viewModel, state = state)
            } else {
                PrivateProfileContent(navController = navController, viewModel = viewModel, state = state)
            }
        }
        is ProfileState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Błąd: ${state.message}")
            }
        }
    }
}

@Composable
fun PublicProfileContent(
    navController: NavController,
    viewModel: ProfileViewModel,
    state: ProfileState.Success
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserAvatar(user = state.targetUser, modifier = Modifier.size(100.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = state.targetUser.username, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = "${state.targetUser.firstName} ${state.targetUser.lastName}", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(24.dp))

        ActionButtons(navController = navController, viewModel = viewModel, state = state)

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Statystyki", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Statystyki są w trakcie implementacji.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.relationshipStatus != RelationshipStatus.SELF) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Interakcje", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { /* TODO: Implement start match */ }) {
                        Text("Rozpocznij mecz")
                    }
                }
            }
        }
    }
}

@Composable
fun PrivateProfileContent(
    navController: NavController,
    viewModel: ProfileViewModel,
    state: ProfileState.Success
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UserAvatar(user = state.targetUser, modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = state.targetUser.username, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(text = "${state.targetUser.firstName} ${state.targetUser.lastName}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Ten profil jest prywatny.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))

        ActionButtons(navController = navController, viewModel = viewModel, state = state)
    }
}

@Composable
fun ActionButtons(
    navController: NavController,
    viewModel: ProfileViewModel,
    state: ProfileState.Success
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val status = state.relationshipStatus
        val buttonText = when (status) {
            RelationshipStatus.SELF -> "Zarządzaj profilem"
            RelationshipStatus.FRIENDS -> "Usuń ze znajomych"
            RelationshipStatus.NOT_FRIENDS, RelationshipStatus.STRANGER -> "Dodaj do znajomych"
            RelationshipStatus.REQUEST_SENT -> "Anuluj zaproszenie"
            RelationshipStatus.REQUEST_RECEIVED -> "Odpowiedz na zaproszenie"
        }

        if (status != RelationshipStatus.REQUEST_RECEIVED) {
            Button(
                onClick = {
                    if (status == RelationshipStatus.SELF) {
                        navController.navigate("manage_profile")
                    } else {
                        viewModel.handleFriendAction()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText)
            }
        }

        if (status == RelationshipStatus.REQUEST_RECEIVED) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.handleFriendAction() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Akceptuj")
                }
                Button(
                    onClick = { viewModel.rejectFriendRequest() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Odrzuć")
                }
            }
        }
        if (status != RelationshipStatus.SELF) {
             OutlinedButton(
                onClick = { viewModel.startChat() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Wiadomość")
            }
        }
    }
}

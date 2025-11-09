package com.example.snookerstats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.ui.profile.ProfileState
import com.example.snookerstats.ui.profile.ProfileViewModel

@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.profileState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Przycisk Wstecz
        TextButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Wróć",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Cofnij")
        }

        // Główna zawartość
        when (val profileState = state) {
            is ProfileState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is ProfileState.Error -> Text(
                text = "Błąd: ${profileState.message}",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
            is ProfileState.Success -> {
                if (profileState.canViewProfile) {
                    UserProfileContent(
                        user = profileState.targetUser,
                        status = profileState.relationshipStatus,
                        onActionClick = { viewModel.handleFriendAction() }
                    )
                } else {
                    PrivateProfileContent(
                        user = profileState.targetUser,
                        onAddFriendClick = { viewModel.handleFriendAction() }
                    )
                }
            }
        }
    }
}


@Composable
private fun UserProfileContent(user: User, status: RelationshipStatus, onActionClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Usunięto Spacer z góry

        // Header Section
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = user.username, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(text = "${user.firstName} ${user.lastName}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons Section
        ActionButtons(status = status, onActionClick = onActionClick)
        Spacer(modifier = Modifier.height(24.dp))

        // Stats Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Statystyki", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                StatRow(icon = Icons.Default.EmojiEvents, label = "Rozegrane mecze", value = "0")
                StatRow(icon = Icons.Default.Star, label = "Najwyższy break", value = "0")
                StatRow(icon = Icons.Default.Leaderboard, label = "Wygrane mecze", value = "0%")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Informacje", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                InfoRow(icon = Icons.Default.Shield, label = "Profil", value = if (user.isPublicProfile) "Publiczny" else "Prywatny")
                user.club?.let {
                    InfoRow(icon = Icons.Default.Groups, label = "Klub", value = it)
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(status: RelationshipStatus, onActionClick: () -> Unit) {
    when (status) {
        RelationshipStatus.SELF -> Button(onClick = onActionClick) { Text("Edytuj profil") }
        RelationshipStatus.REQUEST_RECEIVED -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onActionClick) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Akceptuj")
                    }
                    OutlinedButton(onClick = { /* TODO: Handle reject */ }) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Odrzuć")
                    }
                }
                OutlinedButton(onClick = { /* TODO: Navigate to chat */ }) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Wiadomość")
                }
            }
        }
        else -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (status) {
                    RelationshipStatus.FRIENDS -> {
                        Button(onClick = onActionClick) {
                            Icon(Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Usuń znajomego")
                        }
                    }
                    RelationshipStatus.STRANGER -> {
                        Button(onClick = onActionClick) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Dodaj do znajomych")
                        }
                    }
                    RelationshipStatus.REQUEST_SENT -> {
                        OutlinedButton(onClick = onActionClick) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Anuluj zaproszenie")
                        }
                    }
                    else -> {} // Pozostałe przypadki nie mają tu zastosowania
                }
                OutlinedButton(onClick = { /* TODO: Navigate to chat */ }) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Wiadomość")
                }
            }
        }
    }
}

@Composable
private fun PrivateProfileContent(user: User, onAddFriendClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)
    ) {
        Icon(imageVector = Icons.Default.Lock, contentDescription = "Profil prywatny", modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Profil użytkownika ${user.username} jest prywatny",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Dodaj go do znajomych, aby zobaczyć jego statystyki i aktywność.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddFriendClick) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Dodaj do znajomych")
        }
    }
}

@Composable
private fun StatRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

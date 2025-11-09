package com.example.snookerstats.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.snookerstats.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationsScreen(navController: NavController, viewModel: CommunityViewModel = hiltViewModel()) {
    val uiState = viewModel.uiState
    var selectedFilter by remember { mutableStateOf("Otrzymane") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedFilter == "Otrzymane",
                onClick = { selectedFilter = "Otrzymane" },
                label = { Text("Otrzymane") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = selectedFilter == "Wysłane",
                onClick = { selectedFilter = "Wysłane" },
                label = { Text("Wysłane") }
            )
        }

        if (uiState.isLoadingInvites) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            when (selectedFilter) {
                "Otrzymane" -> ReceivedInvitationsScreen(
                    navController = navController,
                    invites = uiState.receivedInvites,
                    onAccept = { userId, username -> viewModel.acceptInvite(userId, username) },
                    onReject = { userId, username -> viewModel.rejectInvite(userId, username) }
                )
                "Wysłane" -> SentInvitationsScreen(
                    navController = navController,
                    invites = uiState.sentInvites,
                    onCancel = { userId, username -> viewModel.cancelInvite(userId, username) }
                )
            }
        }
    }
}

@Composable
fun ReceivedInvitationsScreen(
    navController: NavController,
    invites: List<User>,
    onAccept: (String, String) -> Unit,
    onReject: (String, String) -> Unit
) {
    if (invites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Brak otrzymanych zaproszeń.", textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(invites) { user ->
                InvitationCard(user = user, navController = navController) {
                    Row {
                        IconButton(onClick = { onAccept(user.uid, user.username) }) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Akceptuj zaproszenie",
                                tint = Color(0xFF4CAF50) // Zielony
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { onReject(user.uid, user.username) }) {
                            Icon(
                                imageVector = Icons.Default.PersonRemove,
                                contentDescription = "Odrzuć zaproszenie",
                                tint = Color(0xFFF44336) // Czerwony
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SentInvitationsScreen(
    navController: NavController,
    invites: List<User>,
    onCancel: (String, String) -> Unit
) {
    if (invites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Brak wysłanych zaproszeń.", textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(invites) { user ->
                InvitationCard(user = user, navController = navController) {
                    IconButton(onClick = { onCancel(user.uid, user.username) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Anuluj zaproszenie"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvitationCard(
    user: User,
    navController: NavController,
    actions: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("user_profile/${user.uid}") }, // Dodano kliknięcie na kartę
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "User Avatar",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) { // Dodano wagę, aby tekst zajmował dostępne miejsce
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            // Spacer(modifier = Modifier.weight(1f)) // Usunięto, ponieważ Column ma już weight
            actions()
        }
    }
}

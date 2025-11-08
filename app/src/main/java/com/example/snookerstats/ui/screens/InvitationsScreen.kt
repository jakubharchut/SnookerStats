package com.example.snookerstats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.snookerstats.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationsScreen(viewModel: CommunityViewModel = hiltViewModel()) {
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
                    invites = uiState.receivedInvites,
                    onAccept = { userId, username -> viewModel.acceptInvite(userId, username) },
                    onReject = { userId, username -> viewModel.rejectInvite(userId, username) }
                )
                "Wysłane" -> SentInvitationsScreen(
                    invites = uiState.sentInvites,
                    onCancel = { userId, username -> viewModel.cancelInvite(userId, username) }
                )
            }
        }
    }
}

@Composable
fun ReceivedInvitationsScreen(
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
                InvitationCard(user = user) {
                    Row {
                        Button(onClick = { onAccept(user.uid, user.username) }) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Akceptuj zaproszenie")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { onReject(user.uid, user.username) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Odrzuć zaproszenie")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SentInvitationsScreen(
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
                InvitationCard(user = user) {
                    IconButton(onClick = { onCancel(user.uid, user.username) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Anuluj zaproszenie"
                            // Usunięto tint, aby użyć domyślnego koloru
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
    actions: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Column {
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
            Spacer(modifier = Modifier.weight(1f))
            actions()
        }
    }
}

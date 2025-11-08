package com.example.snookerstats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            modifier = Modifier.fillMaxWidth(),
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
        LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(invites) { user ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(user.username, modifier = Modifier.weight(1f))
                        Button(onClick = { onAccept(user.uid, user.username) }) { Text("Akceptuj") }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { onReject(user.uid, user.username) }) { Text("Odrzuć") }
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
        LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(invites) { user ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(user.username, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { onCancel(user.uid, user.username) }) { Text("Anuluj") }
                    }
                }
            }
        }
    }
}

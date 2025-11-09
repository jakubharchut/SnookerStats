package com.example.snookerstats.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.ui.community.CommunityViewModel
import com.example.snookerstats.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationsScreen(navController: NavController, viewModel: CommunityViewModel) {
    var selectedFilter by remember { mutableStateOf("Otrzymane") }
    val receivedState by viewModel.receivedRequests.collectAsState()
    val sentState by viewModel.sentRequests.collectAsState()

    LaunchedEffect(selectedFilter) {
        if (selectedFilter == "Otrzymane") {
            viewModel.fetchReceivedRequests()
        } else {
            viewModel.fetchSentRequests()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
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

        when (selectedFilter) {
            "Otrzymane" -> InvitationsList(
                navController = navController,
                state = receivedState,
                isReceived = true,
                onAccept = { viewModel.acceptFriendRequest(it) },
                onReject = { viewModel.rejectFriendRequest(it) }
            )
            "Wysłane" -> InvitationsList(
                navController = navController,
                state = sentState,
                isReceived = false,
                onCancel = { viewModel.cancelFriendRequest(it) }
            )
        }
    }
}

@Composable
private fun InvitationsList(
    navController: NavController,
    state: Resource<List<User>>,
    isReceived: Boolean,
    onAccept: (String) -> Unit = {},
    onReject: (String) -> Unit = {},
    onCancel: (String) -> Unit = {}
) {
    when (state) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is Resource.Success -> {
            val invites = state.data
            if (invites.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isReceived) "Brak otrzymanych zaproszeń." else "Brak wysłanych zaproszeń.",
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(invites) { user ->
                        InvitationCard(user = user, navController = navController) {
                            if (isReceived) {
                                Row {
                                    IconButton(onClick = { onAccept(user.uid) }) {
                                        Icon(Icons.Default.PersonAdd, "Akceptuj", tint = Color(0xFF4CAF50))
                                    }
                                    IconButton(onClick = { onReject(user.uid) }) {
                                        Icon(Icons.Default.PersonRemove, "Odrzuć", tint = Color(0xFFF44336))
                                    }
                                }
                            } else {
                                IconButton(onClick = { onCancel(user.uid) }) {
                                    Icon(Icons.Default.Delete, "Anuluj")
                                }
                            }
                        }
                    }
                }
            }
        }
        is Resource.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Błąd: ${state.message}")
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
            .clickable { navController.navigate("user_profile/${user.uid}") },
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                 user.firstName?.let {
                     Text(
                        text = "$it ${user.lastName ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            actions()
        }
    }
}

package com.example.snookerstats.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.ui.theme.SnookerStatsTheme

@Composable
fun PlayerSearchScreen(
    navController: NavController,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    PlayerSearchScreenContent(
        uiState = uiState,
        onQueryChange = { viewModel.onSearchQueryChange(it) },
        onSearchClick = { viewModel.searchUsers() },
        onUserClick = { userId ->
            navController.navigate("user_profile/$userId")
        },
        onInviteClick = { /* TODO: viewModel.sendInvite(it) */ },
        onChatClick = { /* TODO: navController.navigate("chat/$it") */ }
    )
}

@Composable
fun PlayerSearchScreenContent(
    uiState: CommunityUiState,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onUserClick: (String) -> Unit,
    onInviteClick: (String) -> Unit,
    onChatClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onQueryChange,
            label = { Text("Wyszukaj gracza...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchClick() })
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.searchResults) { result ->
                UserCard(
                    user = result.user,
                    status = result.status,
                    onClick = { onUserClick(result.user.uid) },
                    onInviteClick = { onInviteClick(result.user.uid) },
                    onChatClick = { onChatClick(result.user.uid) }
                )
            }
        }
    }
}

@Composable
fun UserCard(
    user: User,
    status: RelationshipStatus,
    onClick: () -> Unit,
    onInviteClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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

            if (status != RelationshipStatus.SELF) {
                Row {
                    // Pokaż ikonę czatu, jeśli użytkownik jest znajomym LUB obcym
                    if (status == RelationshipStatus.FRIENDS || status == RelationshipStatus.STRANGER) {
                        IconButton(onClick = onChatClick) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = "Chat")
                        }
                    }
                    
                    when (status) {
                        RelationshipStatus.STRANGER -> {
                            IconButton(onClick = onInviteClick) {
                                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add friend")
                            }
                        }
                        RelationshipStatus.INVITE_SENT -> {
                            IconButton(onClick = {}, enabled = false) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Invite sent")
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PlayerSearchScreenPreview() {
    SnookerStatsTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            UserCard(
                user = User(uid = "1", username = "qsaliwan", firstName = "Jakub", lastName = "Harchut"),
                status = RelationshipStatus.STRANGER, // Teraz ma obie ikony
                onClick = {}, onInviteClick = {}, onChatClick = {}
            )
            UserCard(
                user = User(uid = "2", username = "friend", firstName = "John", lastName = "Doe"),
                status = RelationshipStatus.FRIENDS, // Ma tylko ikonę czatu
                onClick = {}, onInviteClick = {}, onChatClick = {}
            )
        }
    }
}

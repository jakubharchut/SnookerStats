package com.example.snookerstats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.snookerstats.ui.common.UserAvatar
import com.example.snookerstats.ui.profile.ProfileNavigationEvent
import com.example.snookerstats.ui.profile.ProfileState
import com.example.snookerstats.ui.profile.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.profileState.collectAsState()
    var showRemoveFriendDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is ProfileNavigationEvent.NavigateToChat -> {
                    navController.navigate("conversation/${event.chatId}/${event.otherUserName}")
                }
                is ProfileNavigationEvent.NavigateToMain -> {
                    // Not handled here
                }
            }
        }
    }

    if (showRemoveFriendDialog && state is ProfileState.Success) {
        val targetUser = (state as ProfileState.Success).targetUser
        AlertDialog(
            onDismissRequest = { showRemoveFriendDialog = false },
            title = { Text("Potwierdzenie") },
            text = { Text("Czy na pewno chcesz usunąć ${targetUser.username} ze znajomych?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.handleFriendAction()
                        showRemoveFriendDialog = false
                    }
                ) { Text("Tak, usuń") }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveFriendDialog = false }) { Text("Anuluj") }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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

        when (val profileState = state) {
            is ProfileState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is ProfileState.Error -> Text(
                text = "Błąd: ${profileState.message}",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
            is ProfileState.Success -> {
                val canViewFullProfile = profileState.targetUser.publicProfile ||
                        profileState.relationshipStatus == RelationshipStatus.FRIENDS ||
                        profileState.relationshipStatus == RelationshipStatus.SELF

                if (canViewFullProfile) {
                    UserProfileContent(
                        user = profileState.targetUser,
                        status = profileState.relationshipStatus,
                        onActionClick = {
                            when (profileState.relationshipStatus) {
                                RelationshipStatus.SELF -> navController.navigate("manage_profile")
                                RelationshipStatus.FRIENDS -> showRemoveFriendDialog = true
                                else -> viewModel.handleFriendAction()
                            }
                        },
                        onRejectClick = { viewModel.rejectFriendRequest() },
                        onChatClick = { viewModel.startChat() }
                    )
                } else {
                    PrivateProfileContent(
                        user = profileState.targetUser,
                        status = profileState.relationshipStatus,
                        onActionClick = { viewModel.handleFriendAction() },
                        onRejectClick = { viewModel.rejectFriendRequest() },
                        onChatClick = { viewModel.startChat() }
                    )
                }
            }
        }
    }
}

@Composable
private fun UserProfileContent(
    user: User,
    status: RelationshipStatus,
    onActionClick: () -> Unit,
    onRejectClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        UserAvatar(user = user, modifier = Modifier.size(120.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = user.username, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(text = "${user.firstName} ${user.lastName}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))

        ActionButtons(status = status, onActionClick = onActionClick, onRejectClick = onRejectClick, onChatClick = onChatClick)
        Spacer(modifier = Modifier.height(24.dp))

        if (user.publicProfile || status == RelationshipStatus.FRIENDS || status == RelationshipStatus.SELF) {
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
        }

        if (status != RelationshipStatus.SELF) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Interakcje", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Rozpocznij mecz")
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    status: RelationshipStatus,
    onActionClick: () -> Unit,
    onRejectClick: () -> Unit,
    onChatClick: () -> Unit
) {
    when (status) {
        RelationshipStatus.SELF -> Button(onClick = onActionClick) { Text("Zarządzaj profilem") }
        RelationshipStatus.REQUEST_RECEIVED -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onActionClick) {
                        Icon(Icons.Default.Check, null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Akceptuj")
                    }
                    OutlinedButton(onClick = onRejectClick) {
                        Icon(Icons.Default.Close, null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Odrzuć")
                    }
                }
                OutlinedButton(onClick = onChatClick) {
                    Icon(Icons.Default.Chat, null)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Wiadomość")
                }
            }
        }
        else -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (status) {
                    RelationshipStatus.FRIENDS -> Button(onClick = onActionClick) {
                        Icon(Icons.Default.PersonRemove, null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Usuń znajomego")
                    }
                    RelationshipStatus.STRANGER, RelationshipStatus.NOT_FRIENDS -> Button(onClick = onActionClick) {
                        Icon(Icons.Default.PersonAdd, null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Dodaj do znajomych")
                    }
                    RelationshipStatus.REQUEST_SENT -> OutlinedButton(onClick = onActionClick) {
                        Icon(Icons.Default.Cancel, null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Anuluj zaproszenie")
                    }
                    else -> {}
                }
                OutlinedButton(onClick = onChatClick) {
                    Icon(Icons.Default.Chat, null)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Wiadomość")
                }
            }
        }
    }
}

@Composable
private fun PrivateProfileContent(
    user: User,
    status: RelationshipStatus,
    onActionClick: () -> Unit,
    onRejectClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)
    ) {
        Icon(imageVector = Icons.Default.Lock, contentDescription = "Profil prywatny", modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Profil użytkownika ${user.username} jest prywatny", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Dodaj go do znajomych, aby zobaczyć jego statystyki i aktywność.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when(status) {
                RelationshipStatus.STRANGER, RelationshipStatus.NOT_FRIENDS -> Button(onClick = onActionClick) {
                    Icon(Icons.Default.PersonAdd, null)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Dodaj do znajomych")
                }
                RelationshipStatus.REQUEST_SENT -> OutlinedButton(onClick = onActionClick) {
                    Icon(Icons.Default.Cancel, null)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Anuluj zaproszenie")
                }
                RelationshipStatus.REQUEST_RECEIVED -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onActionClick) {
                        Icon(Icons.Default.Check, null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Akceptuj")
                    }
                    OutlinedButton(onClick = onRejectClick) {
                        Icon(Icons.Default.Close, null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Odrzuć")
                    }
                }
                else -> {}
            }
            OutlinedButton(onClick = onChatClick) {
                Icon(Icons.Default.Chat, null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Wiadomość")
            }
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

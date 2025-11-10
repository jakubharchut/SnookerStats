package com.example.snookerstats.ui.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.ui.common.UserAvatar
import com.example.snookerstats.ui.community.CommunityViewModel
import com.example.snookerstats.util.Resource
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel = hiltViewModel(),
    communityViewModel: CommunityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val friendsState by communityViewModel.friends.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(showSheet) {
        if (showSheet) {
            communityViewModel.fetchFriends()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            if (event is NavigationEvent.NavigateToConversation) {
                showSheet = false // Hide the sheet before navigating
                navController.navigate("conversation/${event.chatId}")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showSheet = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Nowy czat")
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                when (val state = uiState) {
                    is Resource.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is Resource.Success -> {
                        if (state.data.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Brak aktywnych czatów. Naciśnij +, aby rozpocząć rozmowę.")
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
                                items(state.data) { chatWithUser ->
                                    val isUnread = (chatWithUser.chat.unreadCounts[currentUserId] ?: 0) > 0
                                    ChatListItem(
                                        chatWithUserDetails = chatWithUser,
                                        isUnread = isUnread,
                                        onClick = {
                                            navController.navigate("conversation/${chatWithUser.chat.id}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Błąd: ${state.message}")
                        }
                    }
                }
            }
        }

        TopSheet(
            showSheet = showSheet,
            onDismiss = { showSheet = false }
        ) {
            NewChatContent(
                friendsState = friendsState,
                onUserSelected = { userId ->
                    viewModel.onUserClicked(userId)
                }
            )
        }
    }
}

@Composable
fun NewChatContent(
    friendsState: Resource<List<User>>,
    onUserSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Napisz do znajomego",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        when (friendsState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                if (friendsState.data.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Nie masz jeszcze żadnych znajomych.")
                    }
                } else {
                    LazyColumn(modifier = Modifier.navigationBarsPadding()) {
                        items(friendsState.data) { friend ->
                            ListItem(
                                headlineContent = {
                                    Text(friend.username, fontWeight = FontWeight.Bold)
                                },
                                supportingContent = {
                                    Text("${friend.firstName} ${friend.lastName}")
                                },
                                leadingContent = {
                                    UserAvatar(
                                        user = friend,
                                        modifier = Modifier.size(40.dp)
                                    )
                                },
                                modifier = Modifier.clickable { onUserSelected(friend.uid) }
                            )
                        }
                    }
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Błąd ładowania listy znajomych: ${friendsState.message}", color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun ChatListItem(chatWithUserDetails: ChatWithUserDetails, isUnread: Boolean, onClick: () -> Unit) {
    val fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
    val containerColor = if (isUnread) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = "${chatWithUserDetails.otherUser.firstName} ${chatWithUserDetails.otherUser.lastName}",
                    fontWeight = fontWeight,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            supportingContent = {
                Column {
                    Text(
                        text = "@${chatWithUserDetails.otherUser.username}",
                        fontWeight = fontWeight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = chatWithUserDetails.chat.lastMessage ?: "...",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = fontWeight,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            leadingContent = {
                UserAvatar(
                    user = chatWithUserDetails.otherUser,
                    modifier = Modifier.size(40.dp)
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent) // Make ListItem transparent to see Card's color
        )
    }
}

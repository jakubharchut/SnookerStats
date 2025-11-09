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

    LaunchedEffect(showSheet) {
        if (showSheet) {
            communityViewModel.fetchFriends()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            if (event is NavigationEvent.NavigateToConversation) {
                showSheet = false // Hide the sheet before navigating
                navController.navigate("conversation/${event.chatId}/${event.otherUserName}")
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
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(state.data) { chatWithUser ->
                                    ChatListItem(chatWithUserDetails = chatWithUser, onClick = {
                                        navController.navigate("conversation/${chatWithUser.chat.id}/${chatWithUser.otherUser.username}")
                                    })
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
fun ChatListItem(chatWithUserDetails: ChatWithUserDetails, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(chatWithUserDetails.otherUser.username, fontWeight = FontWeight.Bold) },
        supportingContent = {
            Text(
                text = chatWithUserDetails.chat.lastMessage ?: "...",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            UserAvatar(
                user = chatWithUserDetails.otherUser,
                modifier = Modifier.size(40.dp)
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

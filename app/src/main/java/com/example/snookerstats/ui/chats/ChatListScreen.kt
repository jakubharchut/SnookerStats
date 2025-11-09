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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
    var showDialog by remember { mutableStateOf(false) }

    // Fetch friends when the FAB is clicked and dialog is about to be shown
    LaunchedEffect(showDialog) {
        if (showDialog) {
            communityViewModel.fetchFriends()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            if (event is NavigationEvent.NavigateToConversation) {
                // Close the dialog before navigating
                showDialog = false
                navController.navigate("conversation/${event.chatId}/${event.otherUserName}")
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
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
                                    navController.navigate("conversation/${chatWithUser.chat.id}/${chatWithUser.otherUserName}")
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

    if (showDialog) {
        NewChatDialog(
            friendsState = friendsState,
            onDismiss = { showDialog = false },
            onUserSelected = { userId ->
                viewModel.onUserClicked(userId)
                // Navigation is now handled by LaunchedEffect
            }
        )
    }
}

@Composable
fun ChatListItem(chatWithUserDetails: ChatWithUserDetails, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = chatWithUserDetails.otherUserName, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = chatWithUserDetails.chat.lastMessage ?: "...",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

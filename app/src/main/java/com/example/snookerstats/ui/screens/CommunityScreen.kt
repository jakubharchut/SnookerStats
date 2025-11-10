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
import androidx.navigation.NavController
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.ui.community.CommunityNavigationEvent
import com.example.snookerstats.ui.community.CommunityViewModel
import com.example.snookerstats.ui.screens.common.UserCard
import com.example.snookerstats.util.Resource
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    navController: NavController,
    viewModel: CommunityViewModel = hiltViewModel(),
    initialTabIndex: Int = 0
) {
    var selectedTabIndex by remember { mutableIntStateOf(initialTabIndex) }
    val tabs = listOf("Szukaj", "Znajomi", "Zaproszenia")
    val friendsState by viewModel.friends.collectAsState()
    val receivedRequestsState by viewModel.receivedRequests.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Efekt do pobierania zaproszeń, gdy ekran jest widoczny
    LaunchedEffect(Unit) {
        viewModel.fetchReceivedRequests()
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) {
            viewModel.fetchFriends()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            if (event is CommunityNavigationEvent.NavigateToConversation) {
                navController.navigate("conversation/${event.chatId}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            if (title == "Zaproszenia") {
                                BadgedBox(badge = {
                                    val receivedRequests = (receivedRequestsState as? Resource.Success)?.data
                                    if (!receivedRequests.isNullOrEmpty()) {
                                        Badge(modifier = Modifier.offset(x = 7.dp)) { Text(receivedRequests.size.toString()) }
                                    }
                                }) {
                                    Text(text = title)
                                }
                            } else {
                                Text(text = title)
                            }
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> PlayerSearchScreen(navController = navController, viewModel = viewModel)
                1 -> {
                    when (val state = friendsState) {
                        is Resource.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is Resource.Success -> {
                            FriendsScreen(
                                navController = navController,
                                viewModel = viewModel,
                                friends = state.data
                            )
                        }
                        is Resource.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = "Błąd: ${state.message}")
                            }
                        }
                    }
                }
                2 -> InvitationsScreen(navController = navController, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FriendsScreen(
    navController: NavController,
    viewModel: CommunityViewModel,
    friends: List<User>
) {
    var showDialog by remember { mutableStateOf(false) }
    var userToRemove by remember { mutableStateOf<User?>(null) }

    if (showDialog && userToRemove != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Potwierdzenie usunięcia znajomego") },
            text = { Text("Czy na pewno chcesz usunąć ${userToRemove?.username} ze znajomych?") },
            confirmButton = {
                TextButton(onClick = {
                    userToRemove?.let { user ->
                        viewModel.removeFriend(user.uid)
                    }
                    showDialog = false
                    userToRemove = null
                }) { Text("Tak") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false; userToRemove = null }) { Text("Nie") }
            }
        )
    }

    if (friends.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Nie masz jeszcze żadnych znajomych.", textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(friends) { user ->
                UserCard(
                    user = user,
                    status = RelationshipStatus.FRIENDS,
                    onClick = { navController.navigate("user_profile/${user.uid}") },
                    onActionClick = { 
                        userToRemove = user
                        showDialog = true
                    },
                    onChatClick = { viewModel.startChatWithUser(user) }
                )
            }
        }
    }
}

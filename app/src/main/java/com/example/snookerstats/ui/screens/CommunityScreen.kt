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
import com.example.snookerstats.ui.screens.common.UserCard

@Composable
fun CommunityScreen(
    navController: NavController,
    viewModel: CommunityViewModel = hiltViewModel(),
    initialTabIndex: Int = 0
) {
    var selectedTabIndex by remember { mutableIntStateOf(initialTabIndex) }
    val tabs = listOf("Szukaj", "Znajomi", "Zaproszenia")
    val uiState = viewModel.uiState

    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 0) {
            viewModel.searchUsers()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> PlayerSearchScreen(navController = navController, viewModel = viewModel)
            1 -> FriendsScreen(
                navController = navController,
                viewModel = viewModel,
                friends = uiState.friends
            )
            2 -> InvitationsScreen(navController = navController, viewModel = viewModel)
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
                        viewModel.removeFriend(user.uid, user.username)
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
                    onActionClick = { // To jest akcja dla przycisku usuwania znajomego
                        userToRemove = user
                        showDialog = true
                    },
                    onChatClick = { /* TODO: navController.navigate("chat/${user.uid}") */ }
                )
            }
        }
    }
}

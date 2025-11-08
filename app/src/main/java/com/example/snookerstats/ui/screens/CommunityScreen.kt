package com.example.snookerstats.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding // Dodano ten import
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
import androidx.compose.runtime.mutableIntStateOf // Dodano ten import

@Composable
fun CommunityScreen(navController: NavController, viewModel: CommunityViewModel = hiltViewModel()) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Szukaj", "Znajomi", "Zaproszenia")
    val uiState = viewModel.uiState

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
            1 -> FriendsScreen(friends = uiState.friends, onFriendClick = { /* TODO: Nawigacja do profilu */ })
            2 -> InvitationsScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun FriendsScreen(friends: List<User>, onFriendClick: (String) -> Unit) {
    if (friends.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
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
                // TODO: Użyj UserCard lub podobnego komponentu, gdy będzie dostępny.
                // Na razie prosta implementacja:
                Card(
                    modifier = Modifier.fillMaxSize(),
                    onClick = { onFriendClick(user.uid) }
                ) {
                    Text(text = user.username, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

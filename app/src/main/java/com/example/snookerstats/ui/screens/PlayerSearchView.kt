package com.example.snookerstats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.snookerstats.ui.community.CommunityViewModel
import com.example.snookerstats.ui.screens.common.UserCard
import com.example.snookerstats.util.Resource

@Composable
fun PlayerSearchView(
    navController: NavController,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResultsState by viewModel.searchResults.collectAsState()

    LaunchedEffect(searchQuery) {
        viewModel.onSearchQueryChanged(searchQuery)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Wyszukaj gracza...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                viewModel.onSearchQueryChanged(searchQuery)
            })
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = searchResultsState) {
            is Resource.Loading -> {
                if (searchQuery.length >= 3) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.data) { userWithStatus ->
                        UserCard(
                            user = userWithStatus.user,
                            status = userWithStatus.relationshipStatus,
                            onClick = { navController.navigate("match_setup/${userWithStatus.user.uid}") }, // Zmiana nawigacji na MatchSetup
                            onActionClick = {
                                when (userWithStatus.relationshipStatus) {
                                    RelationshipStatus.NOT_FRIENDS, RelationshipStatus.STRANGER -> viewModel.sendFriendRequest(userWithStatus.user.uid)
                                    RelationshipStatus.FRIENDS -> viewModel.removeFriend(userWithStatus.user.uid)
                                    RelationshipStatus.REQUEST_SENT -> viewModel.cancelFriendRequest(userWithStatus.user.uid)
                                    RelationshipStatus.REQUEST_RECEIVED -> viewModel.acceptFriendRequest(userWithStatus.user.uid)
                                    else -> {}
                                }
                            },
                            onChatClick = { viewModel.startChatWithUser(userWithStatus.user) }
                        )
                    }
                }
            }
            is Resource.Error -> {
                Text(text = "Błąd: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

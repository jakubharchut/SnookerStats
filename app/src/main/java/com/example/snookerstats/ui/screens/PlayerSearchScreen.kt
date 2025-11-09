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
import androidx.navigation.NavController
import com.example.snookerstats.ui.community.CommunityViewModel
import com.example.snookerstats.ui.screens.common.UserCard
import com.example.snookerstats.util.Resource
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PlayerSearchScreen(
    navController: NavController,
    viewModel: CommunityViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResultsState by viewModel.searchResults.collectAsState()

    // A coroutine that launches the search when the user stops typing
    LaunchedEffect(searchQuery) {
        // Debounce logic can be added here if needed
        viewModel.onSearchQueryChanged(searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                if (searchQuery.length >= 3) { // Show indicator only when searching
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.data) { user ->
                        // Assuming RelationshipStatus can be determined or is passed differently
                        // For now, defaulting to NOT_FRIENDS for demonstration
                        UserCard(
                            user = user,
                            status = RelationshipStatus.NOT_FRIENDS, // This needs to be dynamic
                            onClick = { navController.navigate("user_profile/${user.uid}") },
                            onActionClick = { viewModel.sendFriendRequest(user.uid) },
                            onChatClick = { /* TODO */ }
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

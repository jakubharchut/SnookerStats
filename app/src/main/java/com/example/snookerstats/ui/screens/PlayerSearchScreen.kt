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

@Composable
fun PlayerSearchScreen(
    navController: NavController,
    viewModel: CommunityViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResultsState by viewModel.searchResults.collectAsState()

    LaunchedEffect(searchQuery) {
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
                if (searchQuery.length >= 3) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.data) { user ->
                        UserCard(
                            user = user,
                            status = RelationshipStatus.NOT_FRIENDS, // This needs to be dynamic
                            onClick = { navController.navigate("profile/${user.uid}") },
                            onActionClick = { viewModel.sendFriendRequest(user.uid) },
                            onChatClick = { viewModel.startChatWithUser(user) }
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

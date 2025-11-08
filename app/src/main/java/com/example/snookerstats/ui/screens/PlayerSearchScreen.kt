package com.example.snookerstats.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.ui.theme.SnookerStatsTheme

@Composable
fun PlayerSearchScreen(
    navController: NavController,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    PlayerSearchScreenContent(
        uiState = uiState,
        onQueryChange = { viewModel.onSearchQueryChange(it) },
        onSearchClick = { viewModel.searchUsers() },
        onUserClick = { userId ->
            navController.navigate("user_profile/$userId")
        }
    )
}

@Composable
fun PlayerSearchScreenContent(
    uiState: CommunityUiState,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onUserClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = uiState.searchQuery,
            onValueChange = onQueryChange,
            label = { Text("Wyszukaj gracza...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true, // 1. Ustawienie jednej linii
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), // 2. Zmiana przycisku Enter na "Szukaj"
            keyboardActions = KeyboardActions(onSearch = { onSearchClick() }) // 3. Wywołanie akcji po naciśnięciu
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onSearchClick) {
            Text("Szukaj")
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.searchResults) { user ->
                Text(
                    text = user.username,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUserClick(user.uid) }
                        .padding(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerSearchScreenPreview() {
    SnookerStatsTheme {
        PlayerSearchScreenContent(
            uiState = CommunityUiState(
                searchResults = listOf(
                    User(uid = "1", username = "Jakub", email = "a@a.com"),
                    User(uid = "2", username = "Marek", email = "b@b.com")
                )
            ),
            onQueryChange = {},
            onSearchClick = {},
            onUserClick = {}
        )
    }
}

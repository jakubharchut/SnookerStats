package com.example.snookerstats.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.ui.community.CommunityViewModel

@Composable
fun CommunityScreen(viewModel: CommunityViewModel = hiltViewModel()) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val eventMessage by viewModel.eventMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(eventMessage) {
        eventMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearEventMessage()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Logika dodawania gracza gościnnego */ }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj gracza gościnnego")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Wyszukiwarka
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Szukaj graczy...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Szukaj") }
            )

            // Wyniki wyszukiwania
            when (val response = searchResults) {
                is Response.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is Response.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(response.data) { user ->
                            UserSearchResultItem(user = user, onAddFriendClicked = { viewModel.sendFriendRequest(user.uid) })
                        }
                    }
                }
                is Response.Error -> Text("Błąd: ${response.message}", color = androidx.compose.ui.graphics.Color.Red)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sekcja Zaproszeń
            Text("Zaproszenia do znajomych", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            // TODO: Tutaj będzie lista zaproszeń

            Spacer(modifier = Modifier.height(8.dp))

            // Sekcja Znajomych
            Text("Twoi znajomi", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            // TODO: Tutaj będzie lista znajomych
        }
    }
}

@Composable
fun UserSearchResultItem(user: User, onAddFriendClicked: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = user.username.ifEmpty { user.email }, fontSize = 16.sp)
        Button(onClick = onAddFriendClicked) {
            Text("Dodaj")
        }
    }
}

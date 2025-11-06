package com.example.snookerstats.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.ui.community.CommunityViewModel

@Composable
fun CommunityScreen(viewModel: CommunityViewModel = hiltViewModel()) {
    // ... Stany bez zmian ...
    val sentRequests by viewModel.sentRequests.collectAsState()
    val eventMessage by viewModel.eventMessage.collectAsState()
    val context = LocalContext.current

    // ... LaunchedEffect bez zmian ...

    Scaffold(
        // ... bez zmian ...
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // ... Wyszukiwarka bez zmian ...

            if (searchQuery.isNotBlank()) {
                // ... Wyniki wyszukiwania bez zmian ...
            } else {
                // ... Sekcja Zaproszeń Otrzymanych bez zmian ...
                
                Spacer(modifier = Modifier.height(16.dp))

                // Sekcja Zaproszeń Wysłanych
                Text("Wysłane zaproszenia", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                UserList(
                    response = sentRequests,
                    onCancelClicked = { user -> viewModel.cancelFriendRequest(user.uid) }
                )

                // TODO: Dodać listę znajomych
            }
        }
    }
}

@Composable
fun UserList(
    response: Response<List<User>>,
    showButtons: Boolean = false,
    onAddFriendClicked: ((User) -> Unit)? = null,
    onCancelClicked: ((User) -> Unit)? = null
) {
    when (response) {
        is Response.Loading -> CircularProgressIndicator()
        is Response.Success -> {
            if (response.data.isEmpty()) {
                Text("Brak", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(response.data) { user ->
                        UserItem(
                            user = user,
                            showButtons = showButtons,
                            onAddFriendClicked = onAddFriendClicked,
                            onCancelClicked = onCancelClicked
                        )
                    }
                }
            }
        }
        is Response.Error -> Text("Błąd: ${response.message}", color = Color.Red)
    }
}

@Composable
fun UserItem(
    user: User,
    showButtons: Boolean,
    onAddFriendClicked: ((User) -> Unit)? = null,
    onCancelClicked: ((User) -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = user.username.ifEmpty { user.email })
        if (onAddFriendClicked != null) {
            Button(onClick = { onAddFriendClicked(user) }) { Text("Dodaj") }
        }
        if (onCancelClicked != null) {
            Button(onClick = { onCancelClicked(user) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("Anuluj") }
        }
        if (showButtons) {
            Row {
                Button(onClick = { /* TODO: Akceptuj */ }) { Text("Akceptuj") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { /* TODO: Odrzuć */ }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Odrzuć") }
            }
        }
    }
}

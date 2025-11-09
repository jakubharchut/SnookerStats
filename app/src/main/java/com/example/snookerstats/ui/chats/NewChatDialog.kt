package com.example.snookerstats.ui.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.util.Resource

@Composable
fun NewChatDialog(
    friendsState: Resource<List<User>>,
    onDismiss: () -> Unit,
    onUserSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rozpocznij nowy czat") },
        text = {
            when (friendsState) {
                is Resource.Success -> {
                    if (friendsState.data.isEmpty()) {
                        Text("Nie masz jeszcze żadnych znajomych.")
                    } else {
                        LazyColumn {
                            items(friendsState.data) { friend ->
                                Text(
                                    text = friend.username,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onUserSelected(friend.uid) }
                                        .padding(vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> Text("Błąd ładowania listy znajomych: ${friendsState.message}")
                is Resource.Loading -> Text("Ładowanie znajomych...")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

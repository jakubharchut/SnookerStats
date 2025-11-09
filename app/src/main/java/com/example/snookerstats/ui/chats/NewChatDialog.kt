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

@Composable
fun NewChatDialog(
    friends: List<User>,
    onDismiss: () -> Unit,
    onUserSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rozpocznij nowy czat") },
        text = {
            if (friends.isEmpty()) {
                Text("Nie masz jeszcze żadnych znajomych.")
            } else {
                LazyColumn {
                    items(friends) { friend ->
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

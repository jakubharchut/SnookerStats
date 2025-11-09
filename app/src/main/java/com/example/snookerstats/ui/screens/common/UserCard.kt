package com.example.snookerstats.ui.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.ui.common.UserAvatar
import com.example.snookerstats.ui.screens.RelationshipStatus

@Composable
fun UserCard(
    user: User,
    status: RelationshipStatus,
    onClick: () -> Unit,
    onActionClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(user = user, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            if (status != RelationshipStatus.SELF) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Chat Icon is always visible for others
                    IconButton(onClick = onChatClick) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = "Rozpocznij czat")
                    }

                    // Action icon depends on the relationship status
                    when (status) {
                        RelationshipStatus.FRIENDS -> IconButton(onClick = onActionClick) {
                            Icon(imageVector = Icons.Default.PersonRemove, contentDescription = "Usuń znajomego")
                        }
                        RelationshipStatus.NOT_FRIENDS, RelationshipStatus.STRANGER -> IconButton(onClick = onActionClick) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Dodaj znajomego")
                        }
                        RelationshipStatus.REQUEST_SENT -> IconButton(onClick = onActionClick) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = "Anuluj zaproszenie")
                        }
                        RelationshipStatus.REQUEST_RECEIVED -> Row {
                             IconButton(onClick = onActionClick) { // You might need two actions here
                                Icon(Icons.Default.Check, "Akceptuj", tint = Color(0xFF4CAF50))
                            }
                             // Add a reject button if needed
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

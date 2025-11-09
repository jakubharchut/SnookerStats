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
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "User Avatar",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
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
                Row {
                    if (status == RelationshipStatus.FRIENDS) {
                        IconButton(onClick = onChatClick) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = "Chat")
                        }
                        IconButton(onClick = onActionClick) {
                            Icon(imageVector = Icons.Default.PersonRemove, contentDescription = "Remove friend")
                        }
                    } else {
                        IconButton(onClick = onChatClick, enabled = (status == RelationshipStatus.STRANGER || status == RelationshipStatus.REQUEST_SENT) ) {
                             Icon(imageVector = Icons.Default.Chat, contentDescription = "Chat")
                        }
                        when (status) {
                            RelationshipStatus.STRANGER -> {
                                IconButton(onClick = onActionClick) {
                                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add friend")
                                }
                            }
                            RelationshipStatus.REQUEST_SENT -> {
                                IconButton(onClick = {}, enabled = false) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Invite sent")
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

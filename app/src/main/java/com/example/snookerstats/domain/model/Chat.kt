package com.example.snookerstats.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Chat(
    @DocumentId val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String? = null,
    val lastMessageTimestamp: Timestamp? = null,
    val userPresentInChat: String? = null,
    val unreadCounts: Map<String, Int> = emptyMap() // Pole dla liczników nieprzeczytanych wiadomości
)

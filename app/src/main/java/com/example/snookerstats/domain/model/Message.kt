package com.example.snookerstats.domain.model

import com.google.firebase.Timestamp

enum class MessageType {
    TEXT,
    MATCH_SHARE
}

data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val type: MessageType = MessageType.TEXT,
    val matchId: String? = null
)

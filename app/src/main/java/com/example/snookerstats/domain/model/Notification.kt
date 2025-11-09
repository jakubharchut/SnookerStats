package com.example.snookerstats.domain.model

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val userId: String = "", // Do kogo jest to powiadomienie
    val type: NotificationType = NotificationType.GENERIC,
    val title: String = "",
    val message: String = "",
    val senderId: String? = null, // Kto wysłał (np. ID użytkownika przy zaproszeniu)
    val relatedObjectId: String? = null, // ID obiektu powiązanego (np. ID meczu)
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
)

enum class NotificationType {
    GENERIC,
    FRIEND_REQUEST,
    MATCH_INVITATION
}

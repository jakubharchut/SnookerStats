package com.example.snookerstats.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import kotlin.jvm.JvmName

data class Notification(
    val id: String = "",
    val userId: String = "",
    val type: NotificationType = NotificationType.GENERIC,
    val title: String = "",
    val message: String = "",
    val senderId: String? = null,
    val relatedObjectId: String? = null,
    val timestamp: Timestamp = Timestamp.now(),
    
    // Kluczowa poprawka:
    // Używamy @get:PropertyName, aby jawnie wskazać Firebase,
    // jak nazywa się to pole w bazie danych. To rozwiązuje problemy
    // z deserializacją dla pól typu Boolean, których nazwa zaczyna się od "is".
    @get:PropertyName("isRead")
    val isRead: Boolean = false
)

enum class NotificationType {
    GENERIC,
    FRIEND_REQUEST,
    MATCH_INVITATION
}

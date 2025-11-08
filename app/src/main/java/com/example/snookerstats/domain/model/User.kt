package com.example.snookerstats.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val uid: String = "",
    val username: String = "",
    val username_lowercase: String = "",
    val email: String = "",
    // Zmiana na pola obowiązkowe
    val firstName: String = "",
    val firstName_lowercase: String = "",
    val lastName: String = "",
    val lastName_lowercase: String = "",
    val isPublicProfile: Boolean = true,
    val club: String? = null,
    val profileImageUrl: String? = null,
    val friends: List<String> = emptyList(),
    val friendRequestsSent: List<String> = emptyList(),
    val friendRequestsReceived: List<String> = emptyList(),
    val fcmToken: String? = null
)

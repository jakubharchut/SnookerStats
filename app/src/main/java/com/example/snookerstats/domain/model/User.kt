package com.example.snookerstats.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val publicProfile: Boolean = true,
    val club: String? = null,
    val profileImageUrl: String? = null,
    val friends: List<String> = emptyList(),
    val friendRequestsSent: List<String> = emptyList(),
    val friendRequestsReceived: List<String> = emptyList(),
    val favorites: List<String> = emptyList() // DODANE POLE
)

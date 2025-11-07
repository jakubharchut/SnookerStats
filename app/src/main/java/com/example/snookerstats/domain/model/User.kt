package com.example.snookerstats.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String = "",
    val username: String = "",
    val email: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val isPublicProfile: Boolean = false,
    val friends: List<String> = emptyList(),
    val friendRequestsSent: List<String> = emptyList(),
    val friendRequestsReceived: List<String> = emptyList()
)

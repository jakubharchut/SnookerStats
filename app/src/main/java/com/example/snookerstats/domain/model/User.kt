package com.example.snookerstats.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String,
    var username: String = "",
    val email: String = "",
    var firstName: String? = null,
    var lastName: String? = null,
    var club: String? = null,
    var profileImageUrl: String? = null,
    val friends: List<String> = emptyList(),
    val friendRequestsSent: List<String> = emptyList(),
    val friendRequestsReceived: List<String> = emptyList(),
    val isRealNameVisible: Boolean = false
)

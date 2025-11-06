package com.example.snookerstats.domain.model

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val club: String? = null,
    val profileImageUrl: String? = null,
    val friends: List<String> = emptyList(),
    val friendRequestsSent: List<String> = emptyList(),
    val friendRequestsReceived: List<String> = emptyList()
)

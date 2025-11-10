package com.example.snookerstats.ui.screens

import com.example.snookerstats.domain.model.User

data class UserWithStatus(
    val user: User,
    val relationshipStatus: RelationshipStatus
)

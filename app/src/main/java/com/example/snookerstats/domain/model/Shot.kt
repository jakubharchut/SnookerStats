package com.example.snookerstats.domain.model

data class Shot(
    val timestamp: Long,
    val ball: String,
    val points: Int,
    val isFoul: Boolean
)

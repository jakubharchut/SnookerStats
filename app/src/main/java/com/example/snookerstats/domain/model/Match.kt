package com.example.snookerstats.domain.model

data class Match(
    val id: String,
    val player1Id: String,
    val player2Id: String?,
    val date: Long,
    val matchType: String,
    val numberOfReds: Int,
    val status: String,
    val frames: List<Frame>
)

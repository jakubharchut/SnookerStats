package com.example.snookerstats.domain.model

data class Frame(
    val frameNumber: Int,
    val player1Points: Int,
    val player2Points: Int,
    val shots: List<Shot>
)

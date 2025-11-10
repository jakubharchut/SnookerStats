package com.example.snookerstats.domain.model

data class Frame(
    val frameNumber: Int = 1,
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val shots: List<Shot> = emptyList(),
    val breaks: Map<String, List<Int>> = emptyMap(), // Map<playerId, List<breakScore>>
    val winnerId: String? = null
)

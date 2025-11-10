package com.example.snookerstats.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "frames")
data class Frame(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L, // Unique ID for Room
    val frameNumber: Int = 1,
    val matchId: String = "", // Foreign key to Match
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val shots: List<Shot> = emptyList(),
    val breaks: Map<String, List<Int>> = emptyMap(), // Map<playerId, List<breakScore>>
    val winnerId: String? = null
)

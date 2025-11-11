package com.example.snookerstats.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "frames")
data class Frame(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val matchId: String = "",
    val frameNumber: Int = 0,
    val player1Points: Int = 0,
    val player2Points: Int = 0,
    val shots: List<Shot> = emptyList()
)

package com.example.snookerstats.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "frames")
data class Frame(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchId: String,
    val frameNumber: Int,
    val player1Points: Int,
    val player2Points: Int,
    val shots: List<Shot> // Przywrócono listę uderzeń
)

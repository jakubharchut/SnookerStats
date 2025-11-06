package com.example.snookerstats.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey val id: String,
    val player1Id: String,
    val player2Id: String?,
    val date: Long,
    val matchType: String,
    val numberOfReds: Int,
    val status: String,
    val frames: List<Frame> // Przywrócono listę frejmów
)

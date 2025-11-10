package com.example.snookerstats.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey
    val id: String,
    val player1Id: String,
    val player2Id: String?,
    val date: Long,
    val matchType: MatchType,
    val numberOfReds: Int,
    val status: MatchStatus,
    val frames: List<Frame>
)

enum class MatchType {
    RANKING,
    SPARRING
}

enum class MatchStatus {
    IN_PROGRESS,
    COMPLETED
}

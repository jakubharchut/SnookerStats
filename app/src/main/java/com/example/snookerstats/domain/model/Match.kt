package com.example.snookerstats.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey
    val id: String = "",
    val player1Id: String = "",
    val player2Id: String? = null,
    val participants: List<String> = emptyList(),
    val date: Long = 0L,
    val matchType: MatchType = MatchType.SPARRING,
    val numberOfReds: Int = 15,
    val status: MatchStatus = MatchStatus.IN_PROGRESS,
    val frames: List<Frame> = emptyList(),
    val hiddenFor: List<String> = emptyList()
)

enum class MatchType {
    RANKING,
    SPARRING
}

enum class MatchStatus {
    IN_PROGRESS,
    COMPLETED
}

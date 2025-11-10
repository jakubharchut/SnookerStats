package com.example.snookerstats.domain.model

import com.example.snookerstats.ui.screens.MatchFormat
import com.example.snookerstats.ui.screens.MatchType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class MatchStatus {
    IN_PROGRESS,
    COMPLETED
}

data class Match(
    @DocumentId val id: String = "",
    val playerIds: List<String> = emptyList(), // Może zawierać 1 (solo) lub 2 graczy
    val opponentName: String? = null, // Dla graczy-gości
    val date: Timestamp = Timestamp.now(),
    val matchType: MatchType = MatchType.SPARRING,
    val matchFormat: MatchFormat = MatchFormat.FIFTEEN,
    val status: MatchStatus = MatchStatus.IN_PROGRESS,
    
    // Stan meczu na żywo
    val currentFrameIndex: Int = 0,
    val currentPlayerId: String = "",
    val frames: List<Frame> = listOf(Frame()), // Zaczynamy z jednym pustym frejmem
    
    val winnerId: String? = null
)

package com.example.snookerstats.domain.model

import com.google.firebase.Timestamp

enum class ShotType {
    POT, // Wbicie
    FOUL,
    MISS,
    SAFETY,
    END_BREAK // Specjalny typ oznaczający koniec podejścia
}

data class Shot(
    val playerId: String = "",
    val points: Int = 0,
    val type: ShotType = ShotType.POT,
    val timestamp: Timestamp = Timestamp.now()
    // Możemy tu dodać więcej szczegółów, np. która bila, jeśli to wbicie
    // val ball: Ball? = null 
)

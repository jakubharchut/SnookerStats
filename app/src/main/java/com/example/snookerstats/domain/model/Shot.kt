package com.example.snookerstats.domain.model

data class Shot(
    val timestamp: Long = 0L,
    val ballName: String = SnookerBall.Red.name, // Store ball by name
    val points: Int = 0,
    val type: ShotType = ShotType.POTTED
)

enum class ShotType {
    POTTED,
    FOUL,
    SAFETY,
    MISS
}

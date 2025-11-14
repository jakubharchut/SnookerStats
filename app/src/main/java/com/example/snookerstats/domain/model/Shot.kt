package com.example.snookerstats.domain.model

data class Shot(
    val timestamp: Long = 0L,
    val ballName: String = SnookerBall.Red.name,
    val points: Int = 0,
    val type: ShotType = ShotType.POTTED,
    val redsPottedInFoul: Int = 0
)

enum class ShotType {
    POTTED,
    FOUL,
    SAFETY,
    MISS,
    FREE_BALL_POTTED
}

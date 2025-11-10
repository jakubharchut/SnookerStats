package com.example.snookerstats.domain.model

data class Shot(
    val timestamp: Long,
    val ball: SnookerBall,
    val points: Int,
    val type: ShotType
)

enum class ShotType {
    POTTED,
    FOUL,
    SAFETY,
    MISS
}

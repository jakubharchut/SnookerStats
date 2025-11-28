package com.example.snookerstats.ui.screens

import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.User

enum class MatchResult { WIN, LOSS, DRAW }

data class MatchHistoryDisplayItem(
    val match: Match,
    val player1: User?,
    val player2: User?,
    val p1FramesWon: Int,
    val p2FramesWon: Int,
    val result: MatchResult
)

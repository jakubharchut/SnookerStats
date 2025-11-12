package com.example.snookerstats.ui.screens

import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.User

data class MatchHistoryDisplayItem(
    val match: Match,
    val player1: User?,
    val player2: User?,
    val p1FramesWon: Int,
    val p2FramesWon: Int
)

package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.Match
import kotlinx.coroutines.flow.Flow

interface MatchRepository {

    fun getMatchStream(matchId: String): Flow<Match?>

    suspend fun createNewMatch(match: Match)

    suspend fun updateMatch(match: Match)

    // W przyszłości możemy dodać bardziej szczegółowe funkcje
    // suspend fun addShot(matchId: String, shot: Shot)
    // suspend fun endFrame(matchId: String, frame: Frame)
}

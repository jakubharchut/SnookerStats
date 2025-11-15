package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.User
import kotlinx.coroutines.flow.Flow

interface MatchRepository {

    fun getMatchStream(matchId: String): Flow<Match?>

    fun getMatchesForUserStream(userId: String): Flow<List<Match>>

    fun getAllMatchesStream(): Flow<List<Match>>

    fun getOngoingMatch(): Flow<Match?>

    suspend fun createNewMatch(match: Match)

    suspend fun updateMatch(match: Match)

    suspend fun hideMatchForUser(matchId: String, userId: String)
    
    suspend fun deleteMatch(matchId: String)

    suspend fun getPlayersForMatch(player1Id: String, player2Id: String): Pair<User?, User?>

    fun cancelAllJobs()

    // W przyszłości możemy dodać bardziej szczegółowe funkcje
    // suspend fun addShot(matchId: String, shot: Shot)
    // suspend fun endFrame(matchId: String, frame: Frame)
}

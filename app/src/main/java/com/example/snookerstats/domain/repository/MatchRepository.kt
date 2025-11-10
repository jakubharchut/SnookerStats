package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.Shot
import com.example.snookerstats.ui.screens.MatchFormat
import com.example.snookerstats.ui.screens.MatchType
import com.example.snookerstats.util.Resource
import kotlinx.coroutines.flow.Flow

interface MatchRepository {

    suspend fun createNewMatch(
        opponentId: String?, 
        matchType: MatchType, 
        matchFormat: MatchFormat,
        opponentName: String? = null // For guest
    ): Resource<String> // Returns Match ID

    fun getMatchStream(matchId: String): Flow<Resource<Match>>

    suspend fun addShot(matchId: String, frameIndex: Int, shot: Shot): Resource<Unit>

    suspend fun undoLastShot(matchId: String, frameIndex: Int): Resource<Unit>
    
    suspend fun endFrame(matchId: String, frameIndex: Int, winnerId: String): Resource<Unit>

    suspend fun endMatch(matchId: String, winnerId: String): Resource<Unit>
}

package com.example.snookerstats.data.repository

import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.Shot
import com.example.snookerstats.domain.repository.IAuthRepository
import com.example.snookerstats.domain.repository.MatchRepository
import com.example.snookerstats.ui.screens.MatchFormat
import com.example.snookerstats.ui.screens.MatchType
import com.example.snookerstats.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MatchRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: IAuthRepository
) : MatchRepository {

    override suspend fun createNewMatch(
        opponentId: String?,
        matchType: MatchType,
        matchFormat: MatchFormat,
        opponentName: String?
    ): Resource<String> {
        TODO("Not yet implemented")
    }

    override fun getMatchStream(matchId: String): Flow<Resource<Match>> {
        TODO("Not yet implemented")
    }

    override suspend fun addShot(matchId: String, frameIndex: Int, shot: Shot): Resource<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun undoLastShot(matchId: String, frameIndex: Int): Resource<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun endFrame(matchId: String, frameIndex: Int, winnerId: String): Resource<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun endMatch(matchId: String, winnerId: String): Resource<Unit> {
        TODO("Not yet implemented")
    }
}

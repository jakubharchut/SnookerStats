package com.example.snookerstats.data.repository

import com.example.snookerstats.data.local.dao.MatchDao
import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class MatchRepositoryImpl @Inject constructor(
    private val matchDao: MatchDao
    // private val firestore: FirebaseFirestore // Dodamy później
) : MatchRepository {

    override fun getMatchStream(matchId: String): Flow<Match?> {
        // TODO: Na razie zwracamy pusty stream. W przyszłości podłączymy tu Firestore.
        return flowOf(null)
    }

    override suspend fun createNewMatch(match: Match) {
        // TODO: Zapis do Firestore i Room
    }

    override suspend fun updateMatch(match: Match) {
        // TODO: Zapis do Firestore i Room
    }
}

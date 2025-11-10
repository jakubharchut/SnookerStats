package com.example.snookerstats.data.repository

import com.example.snookerstats.data.local.dao.MatchDao
import com.example.snookerstats.domain.model.*
import com.example.snookerstats.domain.repository.MatchRepository
import com.example.snookerstats.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class MatchRepositoryImpl @Inject constructor(
    private val matchDao: MatchDao,
    private val userRepository: UserRepository
    // private val firestore: FirebaseFirestore // Dodamy później
) : MatchRepository {

    override fun getMatchStream(matchId: String): Flow<Match?> {
        // Zwracamy MOCKOWY, ale NOWY mecz (0-0, 0-0)
        val mockMatch = Match(
            id = matchId,
            player1Id = "player1",
            player2Id = "player2",
            date = System.currentTimeMillis(),
            matchType = MatchType.SPARRING,
            numberOfReds = 15,
            status = MatchStatus.IN_PROGRESS,
            frames = emptyList() // Pusta lista frejmów oznacza nowy mecz
        )
        return flowOf(mockMatch)
    }

    // Funkcja do pobierania graczy - upewniamy się, że dane są poprawne
    override suspend fun getPlayersForMatch(player1Id: String, player2Id: String): Pair<User?, User?> {
        val user1 = User(uid = "player1", email = "test1@test.com", username = "koobi", firstName = "Jakub", lastName = "Kowalski")
        val user2 = User(uid = "player2", email = "test2@test.com", username = "Merka", firstName = "Anna", lastName = "Nowak")
        return Pair(user1, user2)
    }


    override suspend fun createNewMatch(match: Match) {
        // TODO: Zapis do Firestore i Room
    }

    override suspend fun updateMatch(match: Match) {
        // TODO: Zapis do Firestore i Room
    }
}

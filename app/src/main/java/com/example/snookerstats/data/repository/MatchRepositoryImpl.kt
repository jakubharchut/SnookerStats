package com.example.snookerstats.data.repository

import com.example.snookerstats.data.local.dao.MatchDao
import com.example.snookerstats.domain.model.*
import com.example.snookerstats.domain.repository.MatchRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val matchDao: MatchDao,
    private val userRepository: UserRepository
    // private val firestore: FirebaseFirestore // Dodamy później
) : MatchRepository {

    // Mock in-memory storage for matches
    private val matches = mutableMapOf<String, Match>()

    override fun getMatchStream(matchId: String): Flow<Match?> {
        // Return a match from our in-memory storage
        return flowOf(matches[matchId])
    }
    
    override suspend fun createNewMatch(match: Match) {
        matches[match.id] = match
    }

    override suspend fun getPlayersForMatch(player1Id: String, player2Id: String): Pair<User?, User?> {
        val user1Result = userRepository.getUser(player1Id)
        val user1 = if (user1Result is Resource.Success) user1Result.data else null

        val user2 = if (player2Id.isNotEmpty()) {
            val user2Result = userRepository.getUser(player2Id)
            if (user2Result is Resource.Success) user2Result.data else null
        } else {
            null
        }
        
        return Pair(user1, user2)
    }

    override suspend fun updateMatch(match: Match) {
        if (matches.containsKey(match.id)) {
            matches[match.id] = match
        }
    }
}

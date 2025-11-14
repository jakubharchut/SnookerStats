package com.example.snookerstats.data.repository

import android.util.Log
import com.example.snookerstats.data.local.dao.MatchDao
import com.example.snookerstats.domain.model.*
import com.example.snookerstats.domain.repository.MatchRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val matchDao: MatchDao,
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore
) : MatchRepository {

    private val matchesCollection = firestore.collection("matches")

    override fun getMatchStream(matchId: String): Flow<Match?> = callbackFlow {
        val listener = matchesCollection.document(matchId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MatchRepository", "Error listening to match stream", error)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val match = snapshot.toObject(Match::class.java)
                    trySend(match).isSuccess
                } else {
                    trySend(null).isSuccess
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getMatchesForUserStream(userId: String): Flow<List<Match>> = callbackFlow {
        val listener = matchesCollection.whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MatchRepository", "Error listening to user matches stream", error)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val matches = snapshot.toObjects(Match::class.java)
                    trySend(matches).isSuccess
                } else {
                    trySend(emptyList()).isSuccess
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getAllMatchesStream(): Flow<List<Match>> = callbackFlow {
        val listener = matchesCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MatchRepository", "Error listening to all matches stream", error)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val matches = snapshot.toObjects(Match::class.java)
                    trySend(matches).isSuccess
                } else {
                    trySend(emptyList()).isSuccess
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getOngoingMatch(): Flow<Match?> {
        return matchDao.getAllMatches().map { matches ->
            val ongoing = matches.firstOrNull { it.status == MatchStatus.IN_PROGRESS }
            Log.d("MatchRepositoryImpl", "Checking for ongoing match. Found: $ongoing")
            ongoing
        }
    }

    override suspend fun createNewMatch(match: Match) {
        try {
            matchesCollection.document(match.id).set(match).await()
            matchDao.insertMatch(match)
        } catch (e: Exception) {
            Log.e("MatchRepository", "Error creating new match", e)
        }
    }

    override suspend fun updateMatch(match: Match) {
        try {
            matchesCollection.document(match.id).set(match).await()
            matchDao.updateMatch(match)
        } catch (e: Exception) {
            Log.e("MatchRepository", "Error updating match", e)
        }
    }

    override suspend fun hideMatchForUser(matchId: String, userId: String) {
        try {
            matchesCollection.document(matchId).update("hiddenFor", FieldValue.arrayUnion(userId)).await()
        } catch (e: Exception) {
            Log.e("MatchRepository", "Error hiding match", e)
        }
    }

    override suspend fun deleteMatch(matchId: String) {
        try {
            matchesCollection.document(matchId).delete().await()
            matchDao.deleteMatchById(matchId)
        } catch (e: Exception) {
            Log.e("MatchRepository", "Error deleting match", e)
        }
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
}

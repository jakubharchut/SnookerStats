package com.example.snookerstats.data.repository

import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.repository.StatsRepository
import com.example.snookerstats.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class StatsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : StatsRepository {

    private val listeners = mutableListOf<ListenerRegistration>()

    override fun getAllMatches(userId: String): Flow<Resource<List<Match>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("matches")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "An error occurred while fetching matches."))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val matches = snapshot.toObjects(Match::class.java)
                    trySend(Resource.Success(matches))
                } else {
                    trySend(Resource.Success(emptyList()))
                }
            }
        listeners.add(listener)
        awaitClose { listener.remove() }
    }

    override fun cancelAllJobs() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }
}
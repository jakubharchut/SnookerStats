package com.example.snookerstats.data.repository

import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.ProfileRepository
import com.example.snookerstats.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProfileRepository {

    private val listeners = mutableListOf<ListenerRegistration>()

    override fun getProfile(userId: String): Flow<Resource<User>> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "An error occurred"))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    if (user != null) {
                        trySend(Resource.Success(user))
                    } else {
                        trySend(Resource.Error("Failed to parse user data."))
                    }
                } else {
                    trySend(Resource.Error("User profile not found."))
                }
            }
        listeners.add(listener)
        awaitClose { listener.remove() }
    }

    override fun getMatches(userId: String): Flow<Resource<List<Match>>> = callbackFlow {
        val listener = firestore.collection("matches")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "An error occurred"))
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

    override suspend fun isUsernameTaken(username: String): Resource<Boolean> {
        return try {
            val query = firestore.collection("users").whereEqualTo("username_lowercase", username.lowercase()).get().await()
            Resource.Success(!query.isEmpty)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun saveUserProfile(user: User): Resource<Unit> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override fun cancelAllJobs() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }
}
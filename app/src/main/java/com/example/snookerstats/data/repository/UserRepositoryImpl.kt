package com.example.snookerstats.data.repository

import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : UserRepository {

    override suspend fun getUser(userId: String): Resource<User> {
        return try {
            val user = firestore.collection("users").document(userId).get().await().toObject(User::class.java)
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("User not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override fun getFriends(): Flow<Resource<List<User>>> = callbackFlow {
        trySend(Resource.Loading)
        val currentUserId = authRepository.currentUser?.uid
        if (currentUserId == null) {
            trySend(Resource.Error("User not logged in."))
            channel.close()
            return@callbackFlow
        }

        val userDocRef = firestore.collection("users").document(currentUserId)
        val listener = userDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Failed to listen for friends."))
                return@addSnapshotListener
            }

            val friendIds = snapshot?.toObject(User::class.java)?.friends
            if (friendIds.isNullOrEmpty()) {
                trySend(Resource.Success(emptyList()))
            } else {
                launch {
                    try {
                        val friendDocs = firestore.collection("users").whereIn("uid", friendIds).get().await()
                        val friends = friendDocs.toObjects(User::class.java)
                        trySend(Resource.Success(friends))
                    } catch (e: Exception) {
                        trySend(Resource.Error(e.message ?: "Failed to fetch friend profiles."))
                    }
                }
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun toggleFavorite(currentUserId: String, favoriteUserId: String) {
        val userDocRef = firestore.collection("users").document(currentUserId)
        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)
                val favorites = snapshot.get("favoriteIds") as? List<String> ?: emptyList()
                if (favorites.contains(favoriteUserId)) {
                    transaction.update(userDocRef, "favoriteIds", FieldValue.arrayRemove(favoriteUserId))
                } else {
                    transaction.update(userDocRef, "favoriteIds", FieldValue.arrayUnion(favoriteUserId))
                }
            }.await()
        } catch (_: Exception) {}
    }

    override fun getCurrentUserId(): String? {
        return authRepository.currentUser?.uid
    }
}
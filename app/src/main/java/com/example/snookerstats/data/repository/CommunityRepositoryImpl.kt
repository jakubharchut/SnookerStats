package com.example.snookerstats.data.repository

import android.util.Log
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.CommunityRepository
import com.example.snookerstats.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CommunityRepository {

    private val currentUserId: String
        get() = auth.currentUser!!.uid

    override suspend fun addToFavorites(userId: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(currentUserId)
                .update("favoriteOpponents", FieldValue.arrayUnion(userId)).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun removeFromFavorites(userId: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(currentUserId)
                .update("favoriteOpponents", FieldValue.arrayRemove(userId)).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override fun searchUsers(query: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading)
        try {
            if (query.isBlank() || query.length < 3) {
                emit(Resource.Success(emptyList()))
                return@flow
            }
            val lowercaseQuery = query.lowercase()
            Log.d("CommunityRepo", "Wyszukiwanie dla: '$lowercaseQuery'")

            coroutineScope {
                val usernameDeferred = async {
                    firestore.collection("users")
                        .whereGreaterThanOrEqualTo("username_lowercase", lowercaseQuery)
                        .whereLessThanOrEqualTo("username_lowercase", lowercaseQuery + "\uf8ff")
                        .get().await().toObjects(User::class.java)
                }
                val firstNameDeferred = async {
                    firestore.collection("users")
                        .whereGreaterThanOrEqualTo("firstName_lowercase", lowercaseQuery)
                        .whereLessThanOrEqualTo("firstName_lowercase", lowercaseQuery + "\uf8ff")
                        .get().await().toObjects(User::class.java)
                }
                val lastNameDeferred = async {
                    firestore.collection("users")
                        .whereGreaterThanOrEqualTo("lastName_lowercase", lowercaseQuery)
                        .whereLessThanOrEqualTo("lastName_lowercase", lowercaseQuery + "\uf8ff")
                        .get().await().toObjects(User::class.java)
                }

                val usernameResults = usernameDeferred.await()
                val firstNameResults = firstNameDeferred.await()
                val lastNameResults = lastNameDeferred.await()

                Log.d("CommunityRepo", "Wyniki - Username: ${usernameResults.size}, Imię: ${firstNameResults.size}, Nazwisko: ${lastNameResults.size}")

                val combinedResults = (usernameResults + firstNameResults + lastNameResults).distinctBy { it.uid }

                emit(Resource.Success(combinedResults))
            }
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Błąd wyszukiwania", e)
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override suspend fun sendFriendRequest(toUserId: String): Resource<Boolean> {
        return try {
            val fromUserId = currentUserId
            if (fromUserId == toUserId) return Resource.Error("Cannot send request to yourself")
            val currentUserRef = firestore.collection("users").document(fromUserId)
            val targetUserRef = firestore.collection("users").document(toUserId)
            firestore.batch()
                .update(currentUserRef, "friendRequestsSent", FieldValue.arrayUnion(toUserId))
                .update(targetUserRef, "friendRequestsReceived", FieldValue.arrayUnion(fromUserId))
                .commit().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun cancelFriendRequest(toUserId: String): Resource<Boolean> {
        return try {
            val fromUserId = currentUserId
            val currentUserRef = firestore.collection("users").document(fromUserId)
            val targetUserRef = firestore.collection("users").document(toUserId)
            firestore.batch()
                .update(currentUserRef, "friendRequestsSent", FieldValue.arrayRemove(toUserId))
                .update(targetUserRef, "friendRequestsReceived", FieldValue.arrayRemove(fromUserId))
                .commit().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun acceptFriendRequest(fromUserId: String): Resource<Boolean> {
        return try {
            val currentUserRef = firestore.collection("users").document(currentUserId)
            val friendUserRef = firestore.collection("users").document(fromUserId)
            firestore.batch()
                .update(currentUserRef, "friends", FieldValue.arrayUnion(fromUserId))
                .update(friendUserRef, "friends", FieldValue.arrayUnion(currentUserId))
                .update(currentUserRef, "friendRequestsReceived", FieldValue.arrayRemove(fromUserId))
                .update(friendUserRef, "friendRequestsSent", FieldValue.arrayRemove(currentUserId))
                .commit().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun rejectFriendRequest(fromUserId: String): Resource<Boolean> {
        return try {
            val currentUserRef = firestore.collection("users").document(currentUserId)
            val friendUserRef = firestore.collection("users").document(fromUserId)
            firestore.batch()
                .update(currentUserRef, "friendRequestsReceived", FieldValue.arrayRemove(fromUserId))
                .update(friendUserRef, "friendRequestsSent", FieldValue.arrayRemove(currentUserId))
                .commit().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun removeFriend(friendId: String): Resource<Boolean> {
        return try {
            val currentUserRef = firestore.collection("users").document(currentUserId)
            val friendUserRef = firestore.collection("users").document(friendId)
            firestore.batch()
                .update(currentUserRef, "friends", FieldValue.arrayRemove(friendId))
                .update(friendUserRef, "friends", FieldValue.arrayRemove(currentUserId))
                .commit().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override fun getFriends(): Flow<Resource<List<User>>> = channelFlow {
        val registration = firestore.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val friendIds = snapshot.toObject<User>()?.friends ?: emptyList()
                    if (friendIds.isEmpty()) {
                        trySend(Resource.Success(emptyList()))
                    } else {
                        launch {
                            try {
                                val users = firestore.collection("users")
                                    .whereIn("uid", friendIds)
                                    .get().await().toObjects(User::class.java)
                                trySend(Resource.Success(users))
                            } catch (e: Exception) {
                                trySend(Resource.Error(e.message ?: "Unknown error"))
                            }
                        }
                    }
                } else {
                    trySend(Resource.Success(emptyList()))
                }
            }
        awaitClose { registration.remove() }
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Unknown error"))
    }

    override fun getReceivedFriendRequests(): Flow<Resource<List<User>>> = channelFlow {
        val registration = firestore.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val requestIds = snapshot.toObject<User>()?.friendRequestsReceived ?: emptyList()
                    if (requestIds.isEmpty()) {
                        trySend(Resource.Success(emptyList()))
                    } else {
                        launch {
                            try {
                                val users = firestore.collection("users")
                                    .whereIn("uid", requestIds)
                                    .get().await().toObjects(User::class.java)
                                trySend(Resource.Success(users))
                            } catch (e: Exception) {
                                trySend(Resource.Error(e.message ?: "Unknown error"))
                            }
                        }
                    }
                } else {
                    trySend(Resource.Success(emptyList()))
                }
            }
        awaitClose { registration.remove() }
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Unknown error"))
    }

    override fun getSentFriendRequests(): Flow<Resource<List<User>>> = channelFlow {
        val registration = firestore.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val requestIds = snapshot.toObject<User>()?.friendRequestsSent ?: emptyList()
                    Log.d("CommunityRepo", "Znaleziono ID wysłanych zaproszeń: $requestIds")
                    if (requestIds.isEmpty()) {
                        trySend(Resource.Success(emptyList()))
                    }
                     else {
                        launch {
                            try {
                                val users = firestore.collection("users")
                                    .whereIn("uid", requestIds)
                                    .get().await().toObjects(User::class.java)
                                Log.d("CommunityRepo", "Pobrano ${users.size} profili dla wysłanych zaproszeń.")
                                trySend(Resource.Success(users))
                            } catch (e: Exception) {
                                Log.e("CommunityRepo", "Błąd w getSentFriendRequests", e)
                                trySend(Resource.Error(e.message ?: "Unknown error"))
                            }
                        }
                    }
                } else {
                    trySend(Resource.Success(emptyList()))
                }
            }
        awaitClose { registration.remove() }
    }.catch { e ->
        emit(Resource.Error(e.message ?: "Unknown error"))
    }
}

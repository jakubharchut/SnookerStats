package com.example.snookerstats.data.repository

import android.util.Log
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.CommunityRepository
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

    override fun searchUsers(query: String): Flow<Response<List<User>>> = flow {
        emit(Response.Loading)
        try {
            if (query.isBlank() || query.length < 3) {
                emit(Response.Success(emptyList()))
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

                emit(Response.Success(combinedResults))
            }
        } catch (e: Exception) {
            Log.e("CommunityRepo", "Błąd wyszukiwania", e)
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    override suspend fun sendFriendRequest(toUserId: String): Response<Boolean> {
        return try {
            val fromUserId = currentUserId
            if (fromUserId == toUserId) return Response.Error("Cannot send request to yourself")
            val currentUserRef = firestore.collection("users").document(fromUserId)
            val targetUserRef = firestore.collection("users").document(toUserId)
            firestore.batch()
                .update(currentUserRef, "friendRequestsSent", FieldValue.arrayUnion(toUserId))
                .update(targetUserRef, "friendRequestsReceived", FieldValue.arrayUnion(fromUserId))
                .commit().await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun cancelFriendRequest(toUserId: String): Response<Boolean> {
        return try {
            val fromUserId = currentUserId
            val currentUserRef = firestore.collection("users").document(fromUserId)
            val targetUserRef = firestore.collection("users").document(toUserId)
            firestore.batch()
                .update(currentUserRef, "friendRequestsSent", FieldValue.arrayRemove(toUserId))
                .update(targetUserRef, "friendRequestsReceived", FieldValue.arrayRemove(fromUserId))
                .commit().await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun acceptFriendRequest(fromUserId: String): Response<Boolean> {
        return try {
            val currentUserRef = firestore.collection("users").document(currentUserId)
            val friendUserRef = firestore.collection("users").document(fromUserId)
            firestore.batch()
                .update(currentUserRef, "friends", FieldValue.arrayUnion(fromUserId))
                .update(friendUserRef, "friends", FieldValue.arrayUnion(currentUserId))
                .update(currentUserRef, "friendRequestsReceived", FieldValue.arrayRemove(fromUserId))
                .update(friendUserRef, "friendRequestsSent", FieldValue.arrayRemove(currentUserId))
                .commit().await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun rejectFriendRequest(fromUserId: String): Response<Boolean> {
        return try {
            val currentUserRef = firestore.collection("users").document(currentUserId)
            val friendUserRef = firestore.collection("users").document(fromUserId)
            firestore.batch()
                .update(currentUserRef, "friendRequestsReceived", FieldValue.arrayRemove(fromUserId))
                .update(friendUserRef, "friendRequestsSent", FieldValue.arrayRemove(currentUserId))
                .commit().await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun removeFriend(friendId: String): Response<Boolean> {
        return try {
            val currentUserRef = firestore.collection("users").document(currentUserId)
            val friendUserRef = firestore.collection("users").document(friendId)
            firestore.batch()
                .update(currentUserRef, "friends", FieldValue.arrayRemove(friendId))
                .update(friendUserRef, "friends", FieldValue.arrayRemove(currentUserId))
                .commit().await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error")
        }
    }

    override fun getFriends(): Flow<Response<List<User>>> = channelFlow {
        val registration = firestore.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Response.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val friendIds = snapshot.toObject<User>()?.friends ?: emptyList()
                    if (friendIds.isEmpty()) {
                        trySend(Response.Success(emptyList()))
                    } else {
                        // Fetch user details for each friend ID within a launched coroutine
                        launch {
                            try {
                                val users = firestore.collection("users")
                                    .whereIn("uid", friendIds)
                                    .get().await().toObjects(User::class.java)
                                trySend(Response.Success(users))
                            } catch (e: Exception) {
                                trySend(Response.Error(e.message ?: "Unknown error"))
                            }
                        }
                    }
                } else {
                    trySend(Response.Success(emptyList()))
                }
            }
        awaitClose { registration.remove() }
    }.catch { e ->
        emit(Response.Error(e.message ?: "Unknown error"))
    }

    override fun getReceivedFriendRequests(): Flow<Response<List<User>>> = channelFlow {
        val registration = firestore.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Response.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val requestIds = snapshot.toObject<User>()?.friendRequestsReceived ?: emptyList()
                    if (requestIds.isEmpty()) {
                        trySend(Response.Success(emptyList()))
                    } else {
                        launch {
                            try {
                                val users = firestore.collection("users")
                                    .whereIn("uid", requestIds)
                                    .get().await().toObjects(User::class.java)
                                trySend(Response.Success(users))
                            } catch (e: Exception) {
                                trySend(Response.Error(e.message ?: "Unknown error"))
                            }
                        }
                    }
                } else {
                    trySend(Response.Success(emptyList()))
                }
            }
        awaitClose { registration.remove() }
    }.catch { e ->
        emit(Response.Error(e.message ?: "Unknown error"))
    }

    override fun getSentFriendRequests(): Flow<Response<List<User>>> = channelFlow {
        val registration = firestore.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Response.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val requestIds = snapshot.toObject<User>()?.friendRequestsSent ?: emptyList()
                    Log.d("CommunityRepo", "Znaleziono ID wysłanych zaproszeń: $requestIds")
                    if (requestIds.isEmpty()) {
                        trySend(Response.Success(emptyList()))
                    }
                     else {
                        launch {
                            try {
                                val users = firestore.collection("users")
                                    .whereIn("uid", requestIds)
                                    .get().await().toObjects(User::class.java)
                                Log.d("CommunityRepo", "Pobrano ${users.size} profili dla wysłanych zaproszeń.")
                                trySend(Response.Success(users))
                            } catch (e: Exception) {
                                Log.e("CommunityRepo", "Błąd w getSentFriendRequests", e)
                                trySend(Response.Error(e.message ?: "Unknown error"))
                            }
                        }
                    }
                } else {
                    trySend(Response.Success(emptyList()))
                }
            }
        awaitClose { registration.remove() }
    }.catch { e ->
        emit(Response.Error(e.message ?: "Unknown error"))
    }
}

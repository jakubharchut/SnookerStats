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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.ktx.snapshots // Keep this import for now, if it's still needed in other places, or remove it if not. We'll replace the direct usage of snapshots.
import com.google.firebase.firestore.SnapshotListenOptions

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

    override fun getFriends(): Flow<Response<List<User>>> = flow {
        // TODO: Implement
    }

    override fun getReceivedFriendRequests(): Flow<Response<List<User>>> = firestore.collection("users").document(currentUserId)
        .snapshots()
        .map { snapshot ->
            val requestIds = snapshot.toObject(User::class.java)?.friendRequestsReceived ?: emptyList()
            if (requestIds.isEmpty()) {
                Response.Success(emptyList<User>()) // Jawne określenie typu
            } else {
                try {
                    val users = firestore.collection("users").whereIn("uid", requestIds).get().await().toObjects(User::class.java)
                    Response.Success(users)
                } catch (e: Exception) {
                    Response.Error(e.message ?: "Unknown error")
                }
            }
        }

    override fun getSentFriendRequests(): Flow<Response<List<User>>> = firestore.collection("users").document(currentUserId)
        .snapshots()
        .map { snapshot ->
            val requestIds = snapshot.toObject(User::class.java)?.friendRequestsSent ?: emptyList()
            Log.d("CommunityRepo", "Znaleziono ID wysłanych zaproszeń: $requestIds")
            if (requestIds.isEmpty()) {
                Response.Success(emptyList<User>()) // Jawne określenie typu
            } else {
                try {
                    val users = firestore.collection("users").whereIn("uid", requestIds).get().await().toObjects(User::class.java)
                    Log.d("CommunityRepo", "Pobrano ${users.size} profili dla wysłanych zaproszeń.")
                    Response.Success(users)
                } catch (e: Exception) {
                    Log.e("CommunityRepo", "Błąd w getSentFriendRequests", e)
                    Response.Error(e.message ?: "Unknown error")
                }
            }
        }
}
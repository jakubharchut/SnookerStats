package com.example.snookerstats.data.repository

import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.CommunityRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CommunityRepository {

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun searchUsers(query: String): Flow<Response<List<User>>> = flow {
        emit(Response.Loading)
        try {
            if (query.isBlank() || query.length < 3) {
                emit(Response.Success(emptyList()))
                return@flow
            }
            val lowercaseQuery = query.lowercase()

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

                val combinedResults = (usernameDeferred.await() + firstNameDeferred.await() + lastNameDeferred.await()).distinctBy { it.uid }
                
                emit(Response.Success(combinedResults))
            }
        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    override suspend fun sendFriendRequest(toUserId: String): Response<Boolean> {
        return try {
            val fromUserId = currentUserId ?: return Response.Error("User not logged in")
            if (fromUserId == toUserId) return Response.Error("Cannot send request to yourself")

            val currentUserRef = firestore.collection("users").document(fromUserId)
            val targetUserRef = firestore.collection("users").document(toUserId)

            firestore.batch()
                .update(currentUserRef, "friendRequestsSent", FieldValue.arrayUnion(toUserId))
                .update(targetUserRef, "friendRequestsReceived", FieldValue.arrayUnion(fromUserId))
                .commit()
                .await()

            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun cancelFriendRequest(toUserId: String): Response<Boolean> {
        return try {
            val fromUserId = currentUserId ?: return Response.Error("User not logged in")

            val currentUserRef = firestore.collection("users").document(fromUserId)
            val targetUserRef = firestore.collection("users").document(toUserId)

            firestore.batch()
                .update(currentUserRef, "friendRequestsSent", FieldValue.arrayRemove(toUserId))
                .update(targetUserRef, "friendRequestsReceived", FieldValue.arrayRemove(fromUserId))
                .commit()
                .await()

            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun acceptFriendRequest(fromUserId: String): Response<Boolean> {
        // TODO: Implement
        return Response.Success(true)
    }

    override suspend fun rejectFriendRequest(fromUserId: String): Response<Boolean> {
        // TODO: Implement
        return Response.Success(true)
    }

    override fun getFriends(): Flow<Response<List<User>>> = flow {
        // TODO: Implement
    }

    override fun getReceivedFriendRequests(): Flow<Response<List<User>>> = flow {
        // TODO: Implement
    }

    override fun getSentFriendRequests(): Flow<Response<List<User>>> = flow {
        // TODO: Implement
    }
}

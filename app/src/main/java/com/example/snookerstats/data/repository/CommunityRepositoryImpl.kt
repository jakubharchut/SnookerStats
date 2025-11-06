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

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CommunityRepository {

    override fun searchUsers(query: String): Flow<Response<List<User>>> = flow {
        emit(Response.Loading)
        try {
            val currentUserId = auth.currentUser?.uid
            val users = firestore.collection("users")
                .orderBy("username")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()
                .toObjects(User::class.java)
                .filter { it.uid != currentUserId }
            emit(Response.Success(users))
        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    override suspend fun sendFriendRequest(toUserId: String): Response<Boolean> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Response.Error("User not logged in")
            if (currentUserId == toUserId) return Response.Error("Cannot send request to yourself")

            val currentUserRef = firestore.collection("users").document(currentUserId)
            val targetUserRef = firestore.collection("users").document(toUserId)

            firestore.batch()
                .update(currentUserRef, "friendRequestsSent", FieldValue.arrayUnion(toUserId))
                .update(targetUserRef, "friendRequestsReceived", FieldValue.arrayUnion(currentUserId))
                .commit()
                .await()

            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun acceptFriendRequest(fromUserId: String): Response<Boolean> {
        return Response.Success(true) // TODO: Implementacja logiki Firestore
    }

    override suspend fun rejectFriendRequest(fromUserId: String): Response<Boolean> {
        return Response.Success(true) // TODO: Implementacja logiki Firestore
    }

    override fun getFriends(currentUserId: String): Flow<Response<List<User>>> = flow {
        emit(Response.Loading)
        try {
            val userDoc = firestore.collection("users").document(currentUserId).get().await()
            val friendIds = userDoc.toObject(User::class.java)?.friends ?: emptyList()

            if (friendIds.isNotEmpty()) {
                val friends = firestore.collection("users")
                    .whereIn("uid", friendIds)
                    .get()
                    .await()
                    .toObjects(User::class.java)
                emit(Response.Success(friends))
            } else {
                emit(Response.Success(emptyList()))
            }
        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }

    override fun getReceivedFriendRequests(currentUserId: String): Flow<Response<List<User>>> = flow {
        emit(Response.Loading)
        try {
            val userDoc = firestore.collection("users").document(currentUserId).get().await()
            val requestIds = userDoc.toObject(User::class.java)?.friendRequestsReceived ?: emptyList()

            if (requestIds.isNotEmpty()) {
                val users = firestore.collection("users")
                    .whereIn("uid", requestIds)
                    .get()
                    .await()
                    .toObjects(User::class.java)
                emit(Response.Success(users))
            } else {
                emit(Response.Success(emptyList()))
            }
        } catch (e: Exception) {
            emit(Response.Error(e.message ?: "Unknown error"))
        }
    }
}

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

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // ... searchUsers bez zmian ...

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
        return Response.Success(true) // TODO
    }

    override suspend fun rejectFriendRequest(fromUserId: String): Response<Boolean> {
        return Response.Success(true) // TODO
    }

    // ... getFriends, getReceivedFriendRequests, getSentFriendRequests bez zmian ...
}

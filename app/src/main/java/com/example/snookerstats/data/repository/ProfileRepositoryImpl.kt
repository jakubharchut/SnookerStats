package com.example.snookerstats.data.repository

import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ProfileRepository {

    override suspend fun saveUserProfile(user: User): Response<Boolean> {
        return try {
            val currentUser = auth.currentUser ?: return Response.Error("User not logged in")
            
            val userRef = firestore.collection("users").document(currentUser.uid)
            
            val updates = mapOf(
                "username" to user.username,
                "username_lowercase" to user.username.lowercase(),
                "firstName" to user.firstName,
                "firstName_lowercase" to user.firstName.lowercase(), // Usunięto '?'
                "lastName" to user.lastName,
                "lastName_lowercase" to user.lastName.lowercase(), // Usunięto '?'
                "publicProfile" to user.isPublicProfile
            )

            userRef.update(updates).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun isUsernameTaken(username: String): Response<Boolean> {
        return try {
            val result = firestore.collection("users").whereEqualTo("username_lowercase", username.lowercase()).limit(1).get().await()
            Response.Success(result.isEmpty.not())
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error")
        }
    }
}

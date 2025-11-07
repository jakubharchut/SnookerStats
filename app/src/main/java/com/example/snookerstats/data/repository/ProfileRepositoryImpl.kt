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
            
            // Pobierz istniejące dane, aby ich nie nadpisać
            val userRef = firestore.collection("users").document(currentUser.uid)
            val existingUser = userRef.get().await().toObject(User::class.java)

            val updatedUser = existingUser?.copy(
                username = user.username,
                firstName = user.firstName,
                lastName = user.lastName,
                isPublicProfile = user.isPublicProfile
            ) ?: user.copy(uid = currentUser.uid, email = currentUser.email ?: "")

            userRef.set(updatedUser).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Unknown error")
        }
    }
}

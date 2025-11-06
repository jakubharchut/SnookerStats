package com.example.snookerstats.data.repository

import com.example.snookerstats.data.local.preferences.EncryptedPrefsManager
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val prefsManager: EncryptedPrefsManager
) : AuthRepository {

    override suspend fun registerUser(email: String, password: String): Response<Boolean> {
        // ... (bez zmian)
    }

    override suspend fun loginUser(email: String, password: String): Response<Boolean> {
        // ... (bez zmian)
    }

    override suspend fun updateUserProfile(username: String, firstName: String, lastName: String): Response<Boolean> {
        // ... (bez zmian)
    }

    override suspend fun isUsernameTaken(username: String): Response<Boolean> {
        return try {
            val result = firestore.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()
            Response.Success(!result.isEmpty)
        } catch (e: Exception) {
            Response.Error(e.message ?: "An unknown error occurred.")
        }
    }

    override suspend fun getCurrentUserData(): Response<User?> {
        // ... (bez zmian)
    }

    override fun saveCredentials(email: String, password: String) {
        // ... (bez zmian)
    }

    override fun getSavedCredentials(): Pair<String, String>? {
        // ... (bez zmian)
    }

    override fun clearCredentials() {
        // ... (bez zmian)
    }

    override suspend fun signOut() {
        // ... (bez zmian)
    }
}

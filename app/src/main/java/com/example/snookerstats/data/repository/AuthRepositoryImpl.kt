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
        // ... (implementacja bez zmian)
        return Response.Success(true)
    }

    override suspend fun loginUser(email: String, password: String): Response<Boolean> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            // Usunięto automatyczne zapisywanie
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "An unknown error occurred.")
        }
    }

    override fun saveCredentials(email: String, password: String) {
        prefsManager.saveCredentials(email, password)
    }

    override fun getSavedCredentials(): Pair<String, String>? {
        val email = prefsManager.getEmail()
        val password = prefsManager.getPassword()
        return if (email != null && password != null) {
            Pair(email, password)
        } else {
            null
        }
    }

    override fun clearCredentials() {
        prefsManager.clearCredentials()
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        // Usunięto automatyczne czyszczenie danych
    }
}

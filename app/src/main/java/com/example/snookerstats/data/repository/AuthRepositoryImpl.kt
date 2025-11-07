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
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Response.Error("Failed to create user.")

            firebaseUser.sendEmailVerification().await()

            val user = User(
                uid = firebaseUser.uid,
                email = email,
                username = ""
            )
            firestore.collection("users").document(firebaseUser.uid).set(user).await()

            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "An unknown error occurred.")
        }
    }

    override suspend fun loginUser(email: String, password: String): Response<Boolean> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "An unknown error occurred.")
        }
    }

    override suspend fun updateUserProfile(username: String, firstName: String, lastName: String): Response<Boolean> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return Response.Error("User not logged in.")
            val userUpdates = mapOf(
                "username" to username,
                "firstName" to firstName,
                "lastName" to lastName
            )
            firestore.collection("users").document(firebaseUser.uid).update(userUpdates).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "An unknown error occurred.")
        }
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
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val user = firestore.collection("users").document(firebaseUser.uid).get().await().toObject(User::class.java)
                Response.Success(user)
            } else {
                Response.Error("User not logged in.")
            }
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
    }
}

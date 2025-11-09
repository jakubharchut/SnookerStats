package com.example.snookerstats.data.repository

import com.example.snookerstats.data.local.preferences.EncryptedPrefsManager
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
                uid = firebaseUser.uid, email = email, username = "", username_lowercase = "",
                firstName = "", firstName_lowercase = "", lastName = "", lastName_lowercase = "", publicProfile = true
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

    override fun getUserProfile(uid: String): Flow<Response<User>> = callbackFlow {
        trySend(Response.Loading)
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Response.Error(error.message ?: "An error occurred"))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    if (user != null) {
                        trySend(Response.Success(user))
                    } else {
                        trySend(Response.Error("Failed to parse user data."))
                    }
                } else {
                    trySend(Response.Error("User profile not found."))
                }
            }
        awaitClose { listener.remove() }
    }


    override suspend fun updateFcmToken(token: String): Response<Boolean> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Response.Error("User not logged in.")
            firestore.collection("users").document(userId).update("fcmToken", token).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Failed to update FCM token.")
        }
    }

    override suspend fun updateProfileVisibility(isPublic: Boolean): Response<Unit> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Response.Error("User not logged in.")
            firestore.collection("users").document(userId).update("publicProfile", isPublic).await()
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Error(e.message ?: "Failed to update profile visibility.")
        }
    }

    override fun saveCredentials(email: String, password: String) {
        prefsManager.saveCredentials(email, password)
    }

    override fun getSavedCredentials(): Pair<String, String>? {
        val email = prefsManager.getEmail()
        val password = prefsManager.getPassword()
        return if (email != null && password != null) Pair(email, password) else null
    }



    override fun clearCredentials() {
        prefsManager.clearCredentials()
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}

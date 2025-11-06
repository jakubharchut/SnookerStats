package com.example.snookerstats.data.repository

import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun registerUser(username: String, email: String, password: String): Response<Boolean> {
        return try {
            // 1. Create user in Firebase Auth
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Response.Error("Failed to create user.")

            // 2. Send verification email
            user.sendEmailVerification().await()

            // 3. Create user document in Firestore
            val userDocument = User(uid = user.uid, username = username, email = email)
            firestore.collection("users").document(user.uid).set(userDocument).await()

            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "An unknown error occurred.")
        }
    }
}

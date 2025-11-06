package com.example.snookerstats.data.repository

import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun registerUser(email: String, password: String): Response<Boolean> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.sendEmailVerification()?.await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Error(e.message ?: "An unknown error occurred.")
        }
    }
}

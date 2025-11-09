package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun registerUser(email: String, password: String): Response<Boolean>
    suspend fun loginUser(email: String, password: String): Response<Boolean>
    fun getUserProfile(uid: String): Flow<Response<User>>
    suspend fun updateFcmToken(token: String): Response<Boolean>
    suspend fun updateProfileVisibility(isPublic: Boolean): Response<Unit>
    fun saveCredentials(email: String, password: String)
    fun getSavedCredentials(): Pair<String, String>?
    fun clearCredentials()
    suspend fun signOut()
}

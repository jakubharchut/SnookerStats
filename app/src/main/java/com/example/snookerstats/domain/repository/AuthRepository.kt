package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.User
import com.example.snookerstats.util.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun registerUser(email: String, password: String): Resource<Boolean>
    suspend fun loginUser(email: String, password: String): Resource<Boolean>
    fun getUserProfile(uid: String): Flow<Resource<User>>
    suspend fun updateFcmToken(token: String): Resource<Boolean>
    suspend fun updateProfileVisibility(isPublic: Boolean): Resource<Unit>
    fun saveCredentials(email: String, password: String)
    fun getSavedCredentials(): Pair<String, String>?
    fun clearCredentials()
    suspend fun signOut()
    fun cancelAllJobs()
}

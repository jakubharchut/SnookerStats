package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User

interface AuthRepository {
    suspend fun registerUser(email: String, password: String): Response<Boolean>
    suspend fun loginUser(email: String, password: String): Response<Boolean>
    suspend fun updateUserProfile(username: String, firstName: String, lastName: String): Response<Boolean>
    fun saveCredentials(email: String, password: String)
    fun getSavedCredentials(): Pair<String, String>?
    fun clearCredentials()
    suspend fun signOut()
    suspend fun getCurrentUserData(): Response<User?>
}

package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.Response

interface AuthRepository {
    suspend fun registerUser(email: String, password: String): Response<Boolean>
    suspend fun loginUser(email: String, password: String): Response<Boolean>
    fun saveCredentials(email: String, password: String)
    fun getSavedCredentials(): Pair<String, String>?
    fun clearCredentials()
    suspend fun signOut() // Dodana ponownie, bo jest częścią czyszczenia creds
}
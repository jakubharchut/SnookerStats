package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun registerUser(email: String, password: String): Response<Boolean>
    suspend fun loginUser(email: String, password: String): Response<Boolean>
    fun getUserProfile(uid: String): Flow<Response<User>> // Nowa funkcja
}

package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.Response

interface AuthRepository {
    suspend fun registerUser(email: String, password: String): Response<Boolean>
}

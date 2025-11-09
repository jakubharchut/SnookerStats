package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.User
import com.example.snookerstats.util.Resource

interface UserRepository {
    suspend fun getUser(userId: String): Resource<User>
    fun getCurrentUserId(): String?
}

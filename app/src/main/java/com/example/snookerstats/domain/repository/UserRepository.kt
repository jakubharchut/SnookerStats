package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.User
import com.example.snookerstats.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUser(userId: String): Resource<User>
    fun getFriends(): Flow<Resource<List<User>>>
    suspend fun toggleFavorite(currentUserId: String, favoriteUserId: String)
    fun getCurrentUserId(): String?
}

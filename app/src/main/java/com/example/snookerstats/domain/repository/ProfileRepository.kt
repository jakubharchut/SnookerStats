package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfile(userId: String): Flow<Resource<User>>
    fun getMatches(userId: String): Flow<Resource<List<Match>>>
    suspend fun isUsernameTaken(username: String): Resource<Boolean>
    suspend fun saveUserProfile(user: User): Resource<Unit>
    fun cancelAllJobs()
}
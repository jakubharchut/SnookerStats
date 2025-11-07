package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User

interface ProfileRepository {
    suspend fun saveUserProfile(user: User): Response<Boolean>
}

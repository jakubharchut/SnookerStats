package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun searchUsers(query: String): Flow<Response<List<User>>>
    suspend fun sendFriendRequest(toUserId: String): Response<Boolean>
    suspend fun acceptFriendRequest(fromUserId: String): Response<Boolean>
    suspend fun rejectFriendRequest(fromUserId: String): Response<Boolean>
    fun getFriends(currentUserId: String): Flow<Response<List<User>>>
    fun getReceivedFriendRequests(currentUserId: String): Flow<Response<List<User>>>
}

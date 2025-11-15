package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.User
import com.example.snookerstats.util.Resource
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun searchUsers(query: String): Flow<Resource<List<User>>>
    suspend fun sendFriendRequest(toUserId: String): Resource<Boolean>
    suspend fun acceptFriendRequest(fromUserId: String): Resource<Boolean>
    suspend fun rejectFriendRequest(fromUserId: String): Resource<Boolean>
    suspend fun cancelFriendRequest(toUserId: String): Resource<Boolean>
    suspend fun removeFriend(friendId: String): Resource<Boolean>
    fun getFriends(): Flow<Resource<List<User>>>
    fun getReceivedFriendRequests(): Flow<Resource<List<User>>>
    fun getSentFriendRequests(): Flow<Resource<List<User>>>
    suspend fun addToFavorites(userId: String): Resource<Unit>
    suspend fun removeFromFavorites(userId: String): Resource<Unit>
    fun cancelAllJobs()
}

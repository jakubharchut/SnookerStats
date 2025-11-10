package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.Chat
import com.example.snookerstats.domain.model.Message
import com.example.snookerstats.util.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChats(): Flow<Resource<List<Chat>>>
    fun getMessages(chatId: String): Flow<Resource<List<Message>>>
    suspend fun sendMessage(chatId: String, message: String): Resource<Unit>
    suspend fun createOrGetChat(otherUserId: String): Resource<String>
    suspend fun deleteChat(chatId: String): Resource<Unit>
    suspend fun updateUserPresenceInChat(chatId: String, userId: String?): Resource<Unit>
    suspend fun resetUnreadCount(chatId: String): Resource<Unit>
}

package com.example.snookerstats.domain.repository

import com.example.snookerstats.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getUnreadNotificationsCount(): Flow<Int>
    fun getAllNotifications(): Flow<List<Notification>>
    suspend fun markNotificationAsRead(notificationId: String)
    suspend fun markAllNotificationsAsRead()
    suspend fun deleteNotification(notificationId: String)
}

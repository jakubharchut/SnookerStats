package com.example.snookerstats.data.repository

import com.example.snookerstats.domain.model.Notification
import com.example.snookerstats.domain.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : NotificationRepository {

    private val currentUser get() = auth.currentUser

    override fun getUnreadNotificationsCount(): Flow<Int> {
        val userId = currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(0)
        return firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .snapshots()
            .map { it.size() }
    }

    override fun getAllNotifications(): Flow<List<Notification>> {
        val userId = currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject<Notification>()?.copy(id = doc.id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }
    }

    override suspend fun markNotificationAsRead(notificationId: String) {
        firestore.collection("notifications").document(notificationId)
            .update("isRead", true).await()
    }

    override suspend fun markAllNotificationsAsRead() {
        val userId = currentUser?.uid ?: return
        val batch = firestore.batch()
        val querySnapshot = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .get().await()

        for (document in querySnapshot.documents) {
            batch.update(document.reference, "isRead", true)
        }
        batch.commit().await()
    }

    override suspend fun deleteNotification(notificationId: String) {
        firestore.collection("notifications").document(notificationId).delete().await()
    }
}

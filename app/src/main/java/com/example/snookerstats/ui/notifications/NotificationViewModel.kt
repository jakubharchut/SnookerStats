package com.example.snookerstats.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Notification
import com.example.snookerstats.domain.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    val unreadNotificationCount: StateFlow<Int> = notificationRepository.getUnreadNotificationsCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private var notificationsJob: Job? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                loadNotifications()
            } else {
                notificationsJob?.cancel()
                _notifications.value = emptyList()
            }
        }
    }

    private fun loadNotifications() {
        notificationsJob?.cancel()
        notificationsJob = viewModelScope.launch {
            notificationRepository.getAllNotifications().collect { notificationList ->
                _notifications.value = notificationList
            }
        }
    }

    fun onNotificationClicked(notification: Notification) {
        viewModelScope.launch {
            if (!notification.isRead) {
                notificationRepository.markNotificationAsRead(notification.id)
                // Stosujemy nasz sprawdzony wzorzec: po zmianie danych, jawnie odświeżamy listę.
                loadNotifications()
            }
        }
    }

    fun onDeleteNotificationConfirmed(notification: Notification) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notification.id)
            // Stosujemy nasz sprawdzony wzorzec: po usunięciu, jawnie odświeżamy listę.
            loadNotifications()
        }
    }
}

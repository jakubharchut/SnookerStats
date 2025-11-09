package com.example.snookerstats.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Notification
import com.example.snookerstats.domain.model.NotificationType
import com.example.snookerstats.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    sealed class NavigationEvent {
        data class NavigateToCommunity(val tabIndex: Int) : NavigationEvent()
    }

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    val unreadNotificationCount: StateFlow<Int> = notificationRepository.getUnreadNotificationsCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
        
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private var notificationsJob: Job? = null

    init {
        loadNotifications()
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
                
                if (notification.type == NotificationType.FRIEND_REQUEST) {
                    _navigationEvent.emit(NavigationEvent.NavigateToCommunity(tabIndex = 2))
                }
                loadNotifications()
            }
        }
    }

    fun onDeleteNotificationConfirmed(notification: Notification) {
        viewModelScope.launch {
            // If this is the last notification, clear the list immediately for better UX
            if (_notifications.value.size == 1) {
                _notifications.value = emptyList()
            }
            notificationRepository.deleteNotification(notification.id)
            loadNotifications()
        }
    }
}

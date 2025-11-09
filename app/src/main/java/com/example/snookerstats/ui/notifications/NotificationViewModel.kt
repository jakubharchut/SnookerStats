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
                // Let the snapshot listener handle the UI update automatically
                if (notification.type == NotificationType.FRIEND_REQUEST) {
                    _navigationEvent.emit(NavigationEvent.NavigateToCommunity(tabIndex = 2))
                }
            }
        }
    }

    fun onDeleteNotificationConfirmed(notification: Notification) {
        viewModelScope.launch {
            // First, perform the delete operation on the repository
            notificationRepository.deleteNotification(notification.id)
            
            // Then, manually update the local list to reflect the change immediately
            // This is a more robust pattern against race conditions with snapshot listeners.
            _notifications.update { currentList ->
                currentList.filterNot { it.id == notification.id }
            }
        }
    }
}

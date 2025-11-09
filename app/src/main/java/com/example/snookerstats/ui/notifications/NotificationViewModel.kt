package com.example.snookerstats.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Notification
import com.example.snookerstats.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    // Ekran będzie bezpośrednio obserwował strumień danych z repozytorium.
    // To jest najbardziej niezawodny sposób na odzwierciedlenie stanu bazy danych.
    val notifications: StateFlow<List<Notification>> = notificationRepository.getAllNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadNotificationCount: StateFlow<Int> = notificationRepository.getUnreadNotificationsCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun onNotificationClicked(notification: Notification) {
        // Jeśli powiadomienie jest nieprzeczytane, wysyłamy polecenie do bazy.
        // UI zaktualizuje się automatycznie, gdy tylko baza danych potwierdzi zapis.
        viewModelScope.launch {
            if (!notification.isRead) {
                notificationRepository.markNotificationAsRead(notification.id)
            }
            // TODO: Dodać logikę nawigacji w zależności od typu powiadomienia
        }
    }

    fun onDeleteNotificationConfirmed(notification: Notification) {
        // Polecenie usunięcia jest wysyłane do bazy.
        // UI zaktualizuje się automatycznie, gdy baza potwierdzi usunięcie.
        viewModelScope.launch {
            notificationRepository.deleteNotification(notification.id)
        }
    }
}

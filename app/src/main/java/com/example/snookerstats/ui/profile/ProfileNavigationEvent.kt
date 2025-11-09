package com.example.snookerstats.ui.profile

sealed class ProfileNavigationEvent {
    object NavigateToMain : ProfileNavigationEvent()
    data class NavigateToChat(val chatId: String) : ProfileNavigationEvent()
}

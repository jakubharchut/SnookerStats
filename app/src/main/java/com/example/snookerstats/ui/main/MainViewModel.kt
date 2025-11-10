package com.example.snookerstats.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.repository.ChatRepository
import com.example.snookerstats.domain.repository.IAuthRepository
import com.example.snookerstats.util.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val chatRepository: ChatRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _username = MutableStateFlow("Witaj!")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _unreadChatCount = MutableStateFlow(0)
    val unreadChatCount: StateFlow<Int> = _unreadChatCount.asStateFlow()

    init {
        loadCurrentUser()
        observeUnreadChats()
    }

    private fun observeUnreadChats() {
        viewModelScope.launch {
            val currentUserId = firebaseAuth.currentUser?.uid ?: return@launch
            chatRepository.getChats().collectLatest { resource ->
                if (resource is Resource.Success) {
                    val count = resource.data.count { chat ->
                        (chat.unreadCounts[currentUserId] ?: 0) > 0
                    }
                    _unreadChatCount.value = count
                }
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                authRepository.getUserProfile(currentUser.uid).collectLatest { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val user = resource.data
                            if (user.username.isNotBlank()) {
                                _username.value = "Witaj, ${user.username}"
                            } else {
                                _username.value = "Witaj, dokończ profil!"
                            }
                        }
                        is Resource.Error -> {
                            _username.value = "Witaj! (Błąd ładowania profilu)"
                        }
                        else -> {}
                    }
                }
            } else {
                _username.value = "Witaj! (Niezalogowany)"
            }
        }
    }
}

package com.example.snookerstats.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.repository.ChatRepository
import com.example.snookerstats.domain.repository.IAuthRepository
import com.example.snookerstats.domain.repository.MatchRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _username = MutableStateFlow("...")
    val username = _username.asStateFlow()

    private val _unreadChatCount = MutableStateFlow(0)
    val unreadChatCount = _unreadChatCount.asStateFlow()

    private val _ongoingMatch = MutableStateFlow<Match?>(null)
    val ongoingMatch: StateFlow<Match?> = _ongoingMatch.asStateFlow()

    init {
        loadUserData()
        observeUnreadChats()
        observeOngoingMatch()
    }

    private fun observeOngoingMatch() {
        viewModelScope.launch {
            matchRepository.getOngoingMatch().collect { match ->
                Log.d("MainViewModel", "Observed ongoing match: $match")
                _ongoingMatch.value = match
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            authRepository.currentUser?.let { user ->
                when (val result = userRepository.getUser(user.uid)) {
                    is Resource.Success -> {
                        _username.value = result.data?.username ?: "Użytkownik"
                    }
                    else -> _username.value = "Użytkownik"
                }
            }
        }
    }

    private fun observeUnreadChats() {
        viewModelScope.launch {
            val currentUserId = authRepository.currentUser?.uid ?: return@launch
            chatRepository.getChats().collectLatest { resource ->
                if (resource is Resource.Success) {
                    val count = resource.data?.count { chat ->
                        (chat.unreadCounts[currentUserId] ?: 0) > 0
                    } ?: 0
                    _unreadChatCount.value = count
                }
            }
        }
    }
}

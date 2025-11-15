package com.example.snookerstats.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.repository.ChatRepository
import com.example.snookerstats.domain.repository.MatchRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import com.example.snookerstats.util.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val matchRepository: MatchRepository,
    private val chatRepository: ChatRepository,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    val username: StateFlow<String> = flow {
        val user = (userRepository.getUser(authRepository.currentUser!!.uid) as? Resource.Success)?.data
        emit(user?.username ?: "...")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "...")

    val unreadChatCount: StateFlow<Int> = flow {
        chatRepository.getChats().collect { resource ->
            if (resource is Resource.Success) {
                val count = resource.data?.count { chat ->
                    (chat.unreadCounts[authRepository.currentUser!!.uid] ?: 0) > 0
                } ?: 0
                emit(count)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val ongoingMatch: StateFlow<Match?> = matchRepository.getOngoingMatch()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun showSnackbar(message: String) {
        viewModelScope.launch {
            snackbarManager.showMessage(message)
        }
    }
}

package com.example.snookerstats.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Chat
import com.example.snookerstats.domain.repository.ChatRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NavigationEvent {
    data class NavigateToConversation(val chatId: String, val otherUserName: String) : NavigationEvent()
}

data class ChatWithUserDetails(
    val chat: Chat,
    val otherUserName: String
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<Resource<List<ChatWithUserDetails>>>(Resource.Loading)
    val uiState: StateFlow<Resource<List<ChatWithUserDetails>>> = _uiState

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        fetchChats()
    }

    private fun fetchChats() {
        viewModelScope.launch {
            chatRepository.getChats().collect { chatResource ->
                when (chatResource) {
                    is Resource.Success -> {
                        val chatsWithDetails = chatResource.data.mapNotNull { chat ->
                            val otherUserId = chat.participants.firstOrNull { it != userRepository.getCurrentUserId() }
                            if (otherUserId != null) {
                                when(val userResource = userRepository.getUser(otherUserId)) {
                                    is Resource.Success -> ChatWithUserDetails(chat, userResource.data.username)
                                    else -> null
                                }
                            } else null
                        }
                        _uiState.value = Resource.Success(chatsWithDetails)
                    }
                    is Resource.Error -> _uiState.value = Resource.Error(chatResource.message)
                    is Resource.Loading -> _uiState.value = Resource.Loading
                }
            }
        }
    }

    fun onUserClicked(otherUserId: String) {
        viewModelScope.launch {
            when (val result = chatRepository.createOrGetChat(otherUserId)) {
                is Resource.Success -> {
                    val otherUser = userRepository.getUser(otherUserId)
                    if (otherUser is Resource.Success) {
                        _navigationEvent.send(NavigationEvent.NavigateToConversation(result.data, otherUser.data.username))
                    }
                }
                is Resource.Error -> {
                    // TODO: Handle error, e.g. show a snackbar
                }
                else -> {}
            }
        }
    }
}

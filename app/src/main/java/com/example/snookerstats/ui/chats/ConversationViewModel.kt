package com.example.snookerstats.ui.chats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Message
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.IAuthRepository
import com.example.snookerstats.domain.repository.ChatRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val authRepository: IAuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: String = savedStateHandle.get<String>("chatId")!!
    private val currentUserId: String? = authRepository.currentUser?.uid

    private val _messagesState = MutableStateFlow<Resource<List<Message>>>(Resource.Loading)
    val messagesState: StateFlow<Resource<List<Message>>> = _messagesState.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private val _otherUser = MutableStateFlow<User?>(null)
    val otherUser: StateFlow<User?> = _otherUser.asStateFlow()

    init {
        loadMessages()
        loadOtherUserDetails()
    }
    
    fun setUserPresence(isPresent: Boolean) {
        viewModelScope.launch {
            chatRepository.updateUserPresenceInChat(chatId, if (isPresent) currentUserId else null)
        }
    }

    private fun loadOtherUserDetails() {
        viewModelScope.launch {
            val participants = chatId.split("_")
            val otherUserId = participants.firstOrNull { it != currentUserId }

            if (otherUserId != null) {
                when (val result = userRepository.getUser(otherUserId)) {
                    is Resource.Success -> {
                        _otherUser.value = result.data
                    }
                    is Resource.Error -> {
                        // TODO: Handle error
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getMessages(chatId).collect { resource ->
                _messagesState.value = resource
            }
        }
    }

    fun onMessageChange(text: String) {
        _messageText.value = text
    }

    fun sendMessage() {
        val text = _messageText.value.trim()
        if (text.isNotBlank()) {
            viewModelScope.launch {
                when (chatRepository.sendMessage(chatId, text)) {
                    is Resource.Success -> {
                        _messageText.value = ""
                    }
                    is Resource.Error -> {
                        // TODO: Handle error (e.g., show a snackbar)
                    }
                    else -> {}
                }
            }
        }
    }

    fun deleteChat() {
        viewModelScope.launch {
            chatRepository.deleteChat(chatId)
            // Navigation is handled in the view
        }
    }
}

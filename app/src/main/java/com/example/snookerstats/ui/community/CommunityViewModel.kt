package com.example.snookerstats.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.ChatRepository
import com.example.snookerstats.domain.repository.CommunityRepository
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CommunityNavigationEvent {
    data class NavigateToConversation(val chatId: String, val otherUserName: String) : CommunityNavigationEvent()
}

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: CommunityRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<Resource<List<User>>>(Resource.Success(emptyList()))
    val searchResults: StateFlow<Resource<List<User>>> = _searchResults.asStateFlow()

    private val _friends = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val friends: StateFlow<Resource<List<User>>> = _friends.asStateFlow()

    private val _receivedRequests = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val receivedRequests: StateFlow<Resource<List<User>>> = _receivedRequests.asStateFlow()

    private val _sentRequests = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val sentRequests: StateFlow<Resource<List<User>>> = _sentRequests.asStateFlow()

    private val _eventMessage = MutableSharedFlow<String>()
    val eventMessage = _eventMessage.asSharedFlow()

    private val _navigationEvent = Channel<CommunityNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {
            repository.searchUsers(query).collect {
                _searchResults.value = it
            }
        }
    }

    fun fetchFriends() {
        viewModelScope.launch {
            repository.getFriends().collect { result ->
                _friends.value = result
            }
        }
    }

    fun fetchReceivedRequests() {
        viewModelScope.launch {
            repository.getReceivedFriendRequests().collect { result ->
                _receivedRequests.value = result
            }
        }
    }

    fun fetchSentRequests() {
        viewModelScope.launch {
            repository.getSentFriendRequests().collect { result ->
                _sentRequests.value = result
            }
        }
    }

    private fun handleAction(action: suspend () -> Resource<Any>, successMessage: String) {
        viewModelScope.launch {
            when (val response = action()) {
                is Resource.Success -> _eventMessage.emit(successMessage)
                is Resource.Error -> _eventMessage.emit("Błąd: ${response.message}")
                else -> {}
            }
        }
    }

    fun startChatWithUser(user: User) {
        viewModelScope.launch {
            when (val result = chatRepository.createOrGetChat(user.uid)) {
                is Resource.Success -> {
                    _navigationEvent.send(CommunityNavigationEvent.NavigateToConversation(result.data, user.username))
                }
                is Resource.Error -> {
                    _eventMessage.emit("Błąd rozpoczynania czatu: ${result.message}")
                }
                else -> {}
            }
        }
    }

    fun sendFriendRequest(toUserId: String) = handleAction({ repository.sendFriendRequest(toUserId) }, "Zaproszenie wysłane!")
    fun cancelFriendRequest(toUserId: String) = handleAction({ repository.cancelFriendRequest(toUserId) }, "Zaproszenie anulowane.")
    fun acceptFriendRequest(fromUserId: String) = handleAction({ repository.acceptFriendRequest(fromUserId) }, "Zaproszenie zaakceptowane.")
    fun rejectFriendRequest(fromUserId: String) = handleAction({ repository.rejectFriendRequest(fromUserId) }, "Zaproszenie odrzucone.")
    fun removeFriend(friendId: String) = handleAction({ repository.removeFriend(friendId) }, "Znajomy usunięty.")
}

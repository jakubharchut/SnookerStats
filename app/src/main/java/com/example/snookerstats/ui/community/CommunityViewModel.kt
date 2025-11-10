package com.example.snookerstats.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.IAuthRepository
import com.example.snookerstats.domain.repository.ChatRepository
import com.example.snookerstats.domain.repository.CommunityRepository
import com.example.snookerstats.ui.screens.RelationshipStatus
import com.example.snookerstats.ui.screens.UserWithStatus
import com.example.snookerstats.util.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CommunityNavigationEvent {
    data class NavigateToConversation(val chatId: String) : CommunityNavigationEvent()
}

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: CommunityRepository,
    private val chatRepository: ChatRepository,
    private val authRepository: IAuthRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<Resource<List<UserWithStatus>>>(Resource.Success(emptyList()))
    val searchResults: StateFlow<Resource<List<UserWithStatus>>> = _searchResults.asStateFlow()

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

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId != null) {
            viewModelScope.launch {
                authRepository.getUserProfile(currentUserId).collectLatest { resource ->
                    if (resource is Resource.Success) {
                        _currentUser.value = resource.data
                        onSearchQueryChanged(_searchQuery.value)
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.length >= 3) {
                _searchResults.value = Resource.Loading
                repository.searchUsers(query).collect { resource ->
                    _searchResults.value = when (resource) {
                        is Resource.Success -> {
                            val currentUserData = _currentUser.value
                            val usersWithStatus = resource.data.map { user ->
                                UserWithStatus(user, getRelationshipStatus(currentUserData, user))
                            }
                            Resource.Success(usersWithStatus)
                        }
                        is Resource.Error -> Resource.Error(resource.message)
                        else -> Resource.Loading
                    }
                }
            } else {
                _searchResults.value = Resource.Success(emptyList())
            }
        }
    }

    private fun getRelationshipStatus(currentUser: User?, otherUser: User): RelationshipStatus {
        currentUser ?: return RelationshipStatus.STRANGER

        return when {
            currentUser.uid == otherUser.uid -> RelationshipStatus.SELF
            currentUser.friends.contains(otherUser.uid) -> RelationshipStatus.FRIENDS
            currentUser.friendRequestsSent.contains(otherUser.uid) -> RelationshipStatus.REQUEST_SENT
            currentUser.friendRequestsReceived.contains(otherUser.uid) -> RelationshipStatus.REQUEST_RECEIVED
            else -> RelationshipStatus.NOT_FRIENDS
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
                is Resource.Success -> {
                    _eventMessage.emit(successMessage)
                    fetchFriends()
                    fetchReceivedRequests()
                    fetchSentRequests()
                }
                is Resource.Error -> _eventMessage.emit("Błąd: ${response.message}")
                else -> {}
            }
        }
    }

    fun startChatWithUser(user: User) {
        viewModelScope.launch {
            when (val result = chatRepository.createOrGetChat(user.uid)) {
                is Resource.Success -> {
                    _navigationEvent.send(CommunityNavigationEvent.NavigateToConversation(result.data))
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

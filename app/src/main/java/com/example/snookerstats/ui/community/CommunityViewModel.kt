package com.example.snookerstats.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.CommunityRepository
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: CommunityRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<Resource<List<User>>>(Resource.Success(emptyList()))
    val searchResults: StateFlow<Resource<List<User>>> = _searchResults.asStateFlow()

    private val _friends = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val friends: StateFlow<Resource<List<User>>> = _friends.asStateFlow()

    private val _receivedRequests = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val receivedRequests: StateFlow<Resource<List<User>>> = _receivedRequests.asStateFlow()

    private val _sentRequests = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val sentRequests: StateFlow<Resource<List<User>>> = _sentRequests.asStateFlow()

    private val _eventMessage = MutableStateFlow<String?>(null)
    val eventMessage: StateFlow<String?> = _eventMessage.asStateFlow()

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

    fun sendFriendRequest(toUserId: String) {
        viewModelScope.launch {
            when (val response = repository.sendFriendRequest(toUserId)) {
                is Resource.Success -> _eventMessage.value = "Zaproszenie wysłane!"
                is Resource.Error -> _eventMessage.value = response.message
                else -> {}
            }
        }
    }
    
    fun cancelFriendRequest(toUserId: String) {
        viewModelScope.launch {
            when (val response = repository.cancelFriendRequest(toUserId)) {
                is Resource.Success -> _eventMessage.value = "Zaproszenie anulowane."
                is Resource.Error -> _eventMessage.value = response.message
                else -> {}
            }
        }
    }

    fun acceptFriendRequest(fromUserId: String) {
        viewModelScope.launch {
            when (val response = repository.acceptFriendRequest(fromUserId)) {
                is Resource.Success -> _eventMessage.value = "Zaproszenie zaakceptowane."
                is Resource.Error -> _eventMessage.value = response.message
                else -> {}
            }
        }
    }

    fun rejectFriendRequest(fromUserId: String) {
        viewModelScope.launch {
            when (val response = repository.rejectFriendRequest(fromUserId)) {
                is Resource.Success -> _eventMessage.value = "Zaproszenie odrzucone."
                is Resource.Error -> _eventMessage.value = response.message
                else -> {}
            }
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            when (val response = repository.removeFriend(friendId)) {
                is Resource.Success -> _eventMessage.value = "Znajomy usunięty."
                is Resource.Error -> _eventMessage.value = response.message
                else -> {}
            }
        }
    }

    fun clearEventMessage() {
        _eventMessage.value = null
    }
}

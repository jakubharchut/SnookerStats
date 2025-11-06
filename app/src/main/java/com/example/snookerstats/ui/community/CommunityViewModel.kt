package com.example.snookerstats.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.CommunityRepository
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<Response<List<User>>>(Response.Success(emptyList()))
    val searchResults: StateFlow<Response<List<User>>> = _searchResults.asStateFlow()

    // Możemy użyć tego do wyświetlania komunikatów (np. "Zaproszenie wysłane!")
    private val _eventMessage = MutableStateFlow<String?>(null)
    val eventMessage: StateFlow<String?> = _eventMessage.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.length >= 3) {
            searchUsers(query)
        } else if (query.isEmpty()) {
            _searchResults.value = Response.Success(emptyList())
        }
    }

    private fun searchUsers(query: String) {
        viewModelScope.launch {
            repository.searchUsers(query).collect { response ->
                _searchResults.value = response
            }
        }
    }

    fun sendFriendRequest(toUserId: String) {
        viewModelScope.launch {
            when (val response = repository.sendFriendRequest(toUserId)) {
                is Response.Success -> _eventMessage.value = "Zaproszenie wysłane!"
                is Response.Error -> _eventMessage.value = "Błąd: ${response.message}"
                else -> {}
            }
        }
    }

    fun clearEventMessage() {
        _eventMessage.value = null
    }

    // TODO: Dodaj funkcje do akceptowania/odrzucania zaproszeń
}

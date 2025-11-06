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

    // ... inne StateFlow bez zmian ...
    private val _sentRequests = MutableStateFlow<Response<List<User>>>(Response.Success(emptyList()))
    val sentRequests: StateFlow<Response<List<User>>> = _sentRequests.asStateFlow()

    private val _eventMessage = MutableStateFlow<String?>(null)
    val eventMessage: StateFlow<String?> = _eventMessage.asStateFlow()

    init {
        loadRequests()
    }

    private fun loadRequests() {
        // ... bez zmian ...
    }

    fun onSearchQueryChanged(query: String) {
        // ... bez zmian ...
    }

    private fun searchUsers(query: String) {
        // ... bez zmian ...
    }

    fun sendFriendRequest(toUserId: String) {
        viewModelScope.launch {
            when (val response = repository.sendFriendRequest(toUserId)) {
                is Response.Success -> {
                    _eventMessage.value = "Zaproszenie wysłane!"
                    loadRequests()
                }
                is Response.Error -> _eventMessage.value = "Błąd: ${response.message}"
                else -> {}
            }
        }
    }
    
    fun cancelFriendRequest(toUserId: String) {
        viewModelScope.launch {
            when (val response = repository.cancelFriendRequest(toUserId)) {
                is Response.Success -> {
                    _eventMessage.value = "Zaproszenie anulowane."
                    loadRequests()
                }
                is Response.Error -> _eventMessage.value = "Błąd: ${response.message}"
                else -> {}
            }
        }
    }

    fun clearEventMessage() {
        _eventMessage.value = null
    }
}

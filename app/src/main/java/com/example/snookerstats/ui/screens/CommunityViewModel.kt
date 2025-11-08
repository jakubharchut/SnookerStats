package com.example.snookerstats.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.repository.CommunityRepository
import com.example.snookerstats.ui.main.SnackbarManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RelationshipStatus {
    FRIENDS,
    INVITE_SENT,
    STRANGER,
    SELF
}

data class UserSearchResult(
    val user: User,
    val status: RelationshipStatus
)

data class CommunityUiState(
    val searchQuery: String = "",
    val searchResults: List<UserSearchResult> = emptyList(),
    val receivedInvites: List<User> = emptyList(),
    val sentInvites: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingInvites: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    var uiState by mutableStateOf(CommunityUiState())
        private set

    private var searchJob: Job? = null

    init {
        startInvitationListeners()
    }

    fun onSearchQueryChange(query: String) {
        uiState = uiState.copy(searchQuery = query, errorMessage = null)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500L)
            searchUsers()
        }
    }

    fun searchUsers() {
        searchJob?.cancel()
        viewModelScope.launch {
            val query = uiState.searchQuery
            if (query.length < 3) {
                uiState = uiState.copy(searchResults = emptyList())
                return@launch
            }
            Log.d("CommunityViewModel", "Rozpoczynanie wyszukiwania dla: '$query'")

            val currentUserId = firebaseAuth.currentUser?.uid ?: return@launch
            
            try {
                val currentUserResponse = authRepository.getUserProfile(currentUserId).first { it !is Response.Loading }

                if (currentUserResponse !is Response.Success) {
                    val errorMessage = (currentUserResponse as? Response.Error)?.message ?: "Nieznany błąd"
                    Log.e("CommunityViewModel", "Błąd ładowania profilu użytkownika: $errorMessage")
                    uiState = uiState.copy(errorMessage = "Nie można załadować Twojego profilu.", isLoading = false)
                    return@launch
                }
                val currentUser = currentUserResponse.data

                communityRepository.searchUsers(query).collect { response ->
                    when (response) {
                        is Response.Loading -> uiState = uiState.copy(isLoading = true)
                        is Response.Success -> {
                            val results = response.data.map { user ->
                                val status = when {
                                    user.uid == currentUserId -> RelationshipStatus.SELF
                                    currentUser.friends.contains(user.uid) -> RelationshipStatus.FRIENDS
                                    currentUser.friendRequestsSent.contains(user.uid) -> RelationshipStatus.INVITE_SENT
                                    else -> RelationshipStatus.STRANGER
                                }
                                UserSearchResult(user, status)
                            }
                            uiState = uiState.copy(searchResults = results, isLoading = false, errorMessage = null)
                            Log.d("CommunityViewModel", "Stan: Sukces. Znaleziono użytkowników: ${results.size}")
                        }
                        is Response.Error -> {
                            uiState = uiState.copy(errorMessage = response.message, isLoading = false)
                            Log.e("CommunityViewModel", "Stan: Błąd - ${response.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Krytyczny błąd podczas pobierania profilu", e)
                uiState = uiState.copy(errorMessage = "Wystąpił krytyczny błąd podczas ładowania Twojego profilu.", isLoading = false)
            }
        }
    }
    
    fun sendFriendRequest(toUserId: String, username: String) {
        viewModelScope.launch {
            when (val response = communityRepository.sendFriendRequest(toUserId)) {
                is Response.Success -> {
                    snackbarManager.showMessage("Wysłano zaproszenie do $username")
                    val updatedResults = uiState.searchResults.map {
                        if (it.user.uid == toUserId) it.copy(status = RelationshipStatus.INVITE_SENT) else it
                    }
                    uiState = uiState.copy(searchResults = updatedResults)
                }
                is Response.Error -> snackbarManager.showMessage("Błąd: Nie udało się wysłać zaproszenia")
                else -> {}
            }
        }
    }

    private fun startInvitationListeners() {
        uiState = uiState.copy(isLoadingInvites = true)

        viewModelScope.launch {
            communityRepository.getReceivedFriendRequests().collect { response ->
                if (response is Response.Success) {
                    uiState = uiState.copy(receivedInvites = response.data, isLoadingInvites = false)
                }
                // Można dodać obsługę błędów, jeśli to konieczne
            }
        }

        viewModelScope.launch {
            communityRepository.getSentFriendRequests().collect { response ->
                if (response is Response.Success) {
                    uiState = uiState.copy(sentInvites = response.data, isLoadingInvites = false)
                }
                // Można dodać obsługę błędów, jeśli to konieczne
            }
        }
    }

    fun acceptInvite(fromUserId: String, username: String) {
        viewModelScope.launch {
            if (communityRepository.acceptFriendRequest(fromUserId) is Response.Success) {
                snackbarManager.showMessage("Zaakceptowano zaproszenie od $username")
            }
        }
    }

    fun rejectInvite(fromUserId: String, username: String) {
        viewModelScope.launch {
            if (communityRepository.rejectFriendRequest(fromUserId) is Response.Success) {
                snackbarManager.showMessage("Odrzucono zaproszenie od $username")
            }
        }
    }

    fun cancelInvite(toUserId: String, username: String) {
        viewModelScope.launch {
            if (communityRepository.cancelFriendRequest(toUserId) is Response.Success) {
                snackbarManager.showMessage("Anulowano zaproszenie do $username")
            }
        }
    }
}

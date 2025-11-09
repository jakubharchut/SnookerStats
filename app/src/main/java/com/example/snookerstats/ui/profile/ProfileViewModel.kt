package com.example.snookerstats.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.ui.screens.RelationshipStatus
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(
        val targetUser: User,
        val currentUser: User,
        val relationshipStatus: RelationshipStatus
    ) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private var targetUserId: String? = null
    private var currentUserId: String? = null

    init {
        targetUserId = savedStateHandle.get<String>("userId") ?: firebaseAuth.currentUser?.uid
        currentUserId = firebaseAuth.currentUser?.uid

        if (targetUserId != null && currentUserId != null) {
            loadProfileData(targetUserId!!, currentUserId!!)
        } else {
            _profileState.value = ProfileState.Error("Użytkownik nie jest zalogowany lub nie znaleziono ID.")
        }
    }

    fun handleFriendAction() {
        viewModelScope.launch {
            val currentState = _profileState.value
            if (currentState is ProfileState.Success) {
                // Tutaj można dodać logikę w zależności od statusu
                // Na razie zostawiam to puste, ale funkcja już istnieje
            }
        }
    }

    fun toggleProfileVisibility(isPublic: Boolean) {
        viewModelScope.launch {
            if (currentUserId == targetUserId) { // Upewnij się, że tylko właściciel profilu może go zmienić
                when (authRepository.updateProfileVisibility(isPublic)) {
                    is Response.Success -> {
                        // Dane odświeżą się automatycznie dzięki listenerowi
                    }
                    is Response.Error -> {
                        // TODO: Pokaż błąd użytkownikowi, np. za pomocą SnackBar
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadProfileData(targetUserId: String, currentUserId: String) {
        viewModelScope.launch {
            val targetUserFlow = authRepository.getUserProfile(targetUserId)
            val currentUserFlow = authRepository.getUserProfile(currentUserId)

            targetUserFlow.combine(currentUserFlow) { targetUserResponse, currentUserResponse ->
                if (targetUserResponse is Response.Success && currentUserResponse is Response.Success) {
                    val targetUser = targetUserResponse.data
                    val currentUser = currentUserResponse.data

                    val status = when {
                        targetUser.uid == currentUser.uid -> RelationshipStatus.SELF
                        currentUser.friends.contains(targetUser.uid) -> RelationshipStatus.FRIENDS
                        currentUser.friendRequestsSent.contains(targetUser.uid) -> RelationshipStatus.REQUEST_SENT
                        currentUser.friendRequestsReceived.contains(targetUser.uid) -> RelationshipStatus.REQUEST_RECEIVED
                        else -> RelationshipStatus.STRANGER
                    }

                    ProfileState.Success(
                        targetUser = targetUser,
                        currentUser = currentUser,
                        relationshipStatus = status
                    )
                } else if (targetUserResponse is Response.Error) {
                    ProfileState.Error(targetUserResponse.message)
                } else if (currentUserResponse is Response.Error) {
                    ProfileState.Error(currentUserResponse.message)
                } else {
                    ProfileState.Loading
                }
            }.collect { state ->
                _profileState.value = state
            }
        }
    }
}

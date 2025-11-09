package com.example.snookerstats.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.repository.CommunityRepository
import com.example.snookerstats.ui.main.SnackbarManager
import com.example.snookerstats.ui.screens.RelationshipStatus
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
    private val communityRepository: CommunityRepository,
    private val snackbarManager: SnackbarManager,
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
                val targetUser = currentState.targetUser
                val response = when (currentState.relationshipStatus) {
                    RelationshipStatus.STRANGER -> communityRepository.sendFriendRequest(targetUser.uid)
                    RelationshipStatus.FRIENDS -> communityRepository.removeFriend(targetUser.uid)
                    RelationshipStatus.REQUEST_SENT -> communityRepository.cancelFriendRequest(targetUser.uid)
                    RelationshipStatus.REQUEST_RECEIVED -> communityRepository.acceptFriendRequest(targetUser.uid)
                    else -> null
                }
                // Logikę snackbarów można dodać tutaj, jeśli potrzeba
            }
        }
    }

    fun rejectFriendRequest() {
        viewModelScope.launch {
            val currentState = _profileState.value
            if (currentState is ProfileState.Success && currentState.relationshipStatus == RelationshipStatus.REQUEST_RECEIVED) {
                communityRepository.rejectFriendRequest(currentState.targetUser.uid)
            }
        }
    }

    fun toggleProfileVisibility(isPublic: Boolean) {
        viewModelScope.launch {
            if (currentUserId == targetUserId) {
                authRepository.updateProfileVisibility(isPublic)
            }
        }
    }

    private fun loadProfileData(targetUserId: String, currentUserId: String) {
        viewModelScope.launch {
            authRepository.getUserProfile(targetUserId).combine(authRepository.getUserProfile(currentUserId)) { targetUserResponse, currentUserResponse ->
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

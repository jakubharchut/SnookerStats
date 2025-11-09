package com.example.snookerstats.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.repository.ChatRepository
import com.example.snookerstats.domain.repository.CommunityRepository
import com.example.snookerstats.ui.screens.RelationshipStatus
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
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
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _navigationEvent = Channel<ProfileNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val currentUserId: String = authRepository.currentUser!!.uid
    private val targetUserId: String = savedStateHandle.get<String>("userId") ?: currentUserId

    init {
        loadProfileData()
    }

    fun startChat() {
        viewModelScope.launch {
            val state = _profileState.value
            if (state is ProfileState.Success) {
                when (val result = chatRepository.createOrGetChat(state.targetUser.uid)) {
                    is Resource.Success -> {
                        _navigationEvent.send(ProfileNavigationEvent.NavigateToChat(result.data))
                    }
                    is Resource.Error -> {
                        // TODO: Handle error
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleAction(action: suspend () -> Resource<out Any>) {
        viewModelScope.launch {
            action()
        }
    }

    fun handleFriendAction() {
        val currentState = _profileState.value
        if (currentState is ProfileState.Success) {
            when (currentState.relationshipStatus) {
                RelationshipStatus.FRIENDS -> handleAction { communityRepository.removeFriend(currentState.targetUser.uid) }
                RelationshipStatus.NOT_FRIENDS, RelationshipStatus.STRANGER -> handleAction { communityRepository.sendFriendRequest(currentState.targetUser.uid) }
                RelationshipStatus.REQUEST_SENT -> handleAction { communityRepository.cancelFriendRequest(currentState.targetUser.uid) }
                RelationshipStatus.REQUEST_RECEIVED -> handleAction { communityRepository.acceptFriendRequest(currentState.targetUser.uid) }
                else -> {}
            }
        }
    }

    fun rejectFriendRequest() {
        val currentState = _profileState.value
        if (currentState is ProfileState.Success && currentState.relationshipStatus == RelationshipStatus.REQUEST_RECEIVED) {
            handleAction { communityRepository.rejectFriendRequest(currentState.targetUser.uid) }
        }
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            authRepository.getUserProfile(targetUserId).combine(authRepository.getUserProfile(currentUserId)) { targetUserResource, currentUserResource ->
                when {
                    targetUserResource is Resource.Success && currentUserResource is Resource.Success -> {
                        val targetUser = targetUserResource.data
                        val currentUser = currentUserResource.data
                        val status = when {
                            targetUser.uid == currentUser.uid -> RelationshipStatus.SELF
                            currentUser.friends.contains(targetUser.uid) -> RelationshipStatus.FRIENDS
                            currentUser.friendRequestsSent.contains(targetUser.uid) -> RelationshipStatus.REQUEST_SENT
                            currentUser.friendRequestsReceived.contains(targetUser.uid) -> RelationshipStatus.REQUEST_RECEIVED
                            else -> RelationshipStatus.STRANGER
                        }
                        ProfileState.Success(targetUser, currentUser, status)
                    }
                    targetUserResource is Resource.Error -> ProfileState.Error(targetUserResource.message)
                    currentUserResource is Resource.Error -> ProfileState.Error(currentUserResource.message)
                    else -> ProfileState.Loading
                }
            }.collect { state ->
                _profileState.value = state
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
}

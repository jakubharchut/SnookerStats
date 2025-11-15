package com.example.snookerstats.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.ShotType
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.repository.ChatRepository
import com.example.snookerstats.domain.repository.CommunityRepository
import com.example.snookerstats.domain.repository.ProfileRepository
import com.example.snookerstats.ui.screens.RelationshipStatus
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileStats(
    val matchesPlayed: Int = 0,
    val highestBreak: Int = 0,
    val winPercentage: Int = 0
)

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(
        val targetUser: User,
        val currentUser: User,
        val relationshipStatus: RelationshipStatus,
        val stats: ProfileStats
    ) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val communityRepository: CommunityRepository,
    private val chatRepository: ChatRepository,
    private val profileRepository: ProfileRepository,
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
            combine(
                profileRepository.getProfile(targetUserId),
                profileRepository.getProfile(currentUserId),
                profileRepository.getMatches(targetUserId)
            ) { targetUserRes, currentUserRes, matchesRes ->
                // Logic to process the resources
                if (targetUserRes is Resource.Success && currentUserRes is Resource.Success && matchesRes is Resource.Success) {
                    val targetUser = targetUserRes.data
                    val currentUser = currentUserRes.data
                    val matches = matchesRes.data

                    val relationshipStatus = when {
                        targetUser.uid == currentUser.uid -> RelationshipStatus.SELF
                        currentUser.friends.contains(targetUser.uid) -> RelationshipStatus.FRIENDS
                        currentUser.friendRequestsSent.contains(targetUser.uid) -> RelationshipStatus.REQUEST_SENT
                        currentUser.friendRequestsReceived.contains(targetUser.uid) -> RelationshipStatus.REQUEST_RECEIVED
                        else -> RelationshipStatus.STRANGER
                    }

                    var highestBreak = 0
                    matches.forEach { match ->
                        match.frames.forEach { frame ->
                            var currentBreak = 0
                            frame.shots.forEach { shot ->
                                if (shot.playerId == targetUserId && (shot.type == ShotType.POTTED || shot.type == ShotType.FREE_BALL_POTTED)) {
                                    currentBreak += shot.points
                                } else {
                                    if (currentBreak > highestBreak) {
                                        highestBreak = currentBreak
                                    }
                                    currentBreak = 0
                                }
                            }
                            if (currentBreak > highestBreak) {
                                highestBreak = currentBreak
                            }
                        }
                    }

                    val matchesPlayed = matches.size
                    val matchesWon = matches.count { match ->
                        val p1Frames = match.frames.count { it.player1Points > it.player2Points }
                        val p2Frames = match.frames.count { it.player2Points > it.player1Points }
                        (match.player1Id == targetUserId && p1Frames > p2Frames) || (match.player2Id == targetUserId && p2Frames > p1Frames)
                    }
                    val winPercentage = if (matchesPlayed > 0) (matchesWon * 100 / matchesPlayed) else 0

                    val stats = ProfileStats(
                        matchesPlayed = matchesPlayed,
                        highestBreak = highestBreak,
                        winPercentage = winPercentage
                    )

                    ProfileState.Success(targetUser, currentUser, relationshipStatus, stats)
                } else if (targetUserRes is Resource.Error) {
                    ProfileState.Error(targetUserRes.message)
                } else if (currentUserRes is Resource.Error) {
                    ProfileState.Error(currentUserRes.message)
                } else if (matchesRes is Resource.Error) {
                    ProfileState.Error(matchesRes.message)
                } else {
                    ProfileState.Loading
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

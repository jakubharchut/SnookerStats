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
        val relationshipStatus: RelationshipStatus,
        val canViewProfile: Boolean
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

    init {
        // Jeśli userId nie jest przekazany, użyj ID bieżącego użytkownika
        val targetUserId = savedStateHandle.get<String>("userId") ?: firebaseAuth.currentUser?.uid
        val currentUserId = firebaseAuth.currentUser?.uid

        if (targetUserId != null && currentUserId != null) {
            loadProfileData(targetUserId, currentUserId)
        } else {
            // Zmieniono komunikat, aby był bardziej adekwatny
            _profileState.value = ProfileState.Error("Użytkownik nie jest zalogowany lub nie znaleziono ID.")
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
                        currentUser.friendRequestsSent.contains(targetUser.uid) -> RelationshipStatus.INVITE_SENT
                        else -> RelationshipStatus.STRANGER
                    }
                    val canViewProfile = targetUser.isPublicProfile || status == RelationshipStatus.FRIENDS || status == RelationshipStatus.SELF

                    ProfileState.Success(
                        targetUser = targetUser,
                        currentUser = currentUser,
                        relationshipStatus = status,
                        canViewProfile = canViewProfile
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

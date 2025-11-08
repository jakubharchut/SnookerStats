package com.example.snookerstats.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(
        val targetUser: User,
        val currentUser: User,
        val isFriend: Boolean,
        val canViewProfile: Boolean
    ) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val firebaseAuth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        // Sprawdź, czy 'userId' jest przekazany. Jeśli nie, użyj ID bieżącego użytkownika.
        val targetUserId = savedStateHandle.get<String>("userId") ?: firebaseAuth.currentUser?.uid
        val currentUserId = firebaseAuth.currentUser?.uid

        if (targetUserId != null && currentUserId != null) {
            loadProfileData(targetUserId, currentUserId)
        } else {
            _profileState.value = ProfileState.Error("User not found.")
        }
    }

    private fun loadProfileData(targetUserId: String, currentUserId: String) {
        viewModelScope.launch {
            val targetUserFlow = authRepository.getUserProfile(targetUserId)
            val currentUserFlow = authRepository.getUserProfile(currentUserId)

            targetUserFlow.combine(currentUserFlow) { targetUserResponse, currentUserResponse ->
                data class CombinedUsers(val target: Response<User>, val current: Response<User>)
                CombinedUsers(targetUserResponse, currentUserResponse)
            }.collect { combined ->
                val targetUserResponse = combined.target
                val currentUserResponse = combined.current

                if (targetUserResponse is Response.Success && currentUserResponse is Response.Success) {
                    val targetUser = targetUserResponse.data
                    val currentUser = currentUserResponse.data
                    val isFriend = currentUser.friends.contains(targetUser.uid)
                    val canViewProfile = targetUser.isPublicProfile || isFriend || targetUser.uid == currentUser.uid

                    _profileState.value = ProfileState.Success(
                        targetUser = targetUser,
                        currentUser = currentUser,
                        isFriend = isFriend,
                        canViewProfile = canViewProfile
                    )
                } else if (targetUserResponse is Response.Error) {
                    _profileState.value = ProfileState.Error(targetUserResponse.message)
                } else if (currentUserResponse is Response.Error) {
                    _profileState.value = ProfileState.Error(currentUserResponse.message)
                } else {
                    _profileState.value = ProfileState.Loading
                }
            }
        }
    }
}

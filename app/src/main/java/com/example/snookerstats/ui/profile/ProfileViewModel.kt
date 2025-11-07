package com.example.snookerstats.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Success(val message: String) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class ProfileNavigationEvent {
    object NavigateToMain : ProfileNavigationEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState

    private val _navigationEvent = MutableSharedFlow<ProfileNavigationEvent>()
    val navigationEvent: SharedFlow<ProfileNavigationEvent> = _navigationEvent

    fun saveUserProfile(user: User) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading

            // 1. Sprawdź, czy nazwa użytkownika nie jest pusta
            if (user.username.isBlank()) {
                _profileState.value = ProfileState.Error("Nazwa użytkownika nie może być pusta.")
                return@launch
            }

            // 2. Sprawdź, czy nazwa użytkownika jest już zajęta
            when (val isTakenResponse = repo.isUsernameTaken(user.username)) {
                is Response.Success -> {
                    if (isTakenResponse.data) {
                        _profileState.value = ProfileState.Error("Ta nazwa użytkownika jest już zajęta.")
                        return@launch
                    }
                }
                is Response.Error -> {
                    _profileState.value = ProfileState.Error(isTakenResponse.message)
                    return@launch
                }
                else -> {}
            }

            // 3. Jeśli wszystko jest OK, zapisz profil
            when(val saveResponse = repo.saveUserProfile(user)) {
                is Response.Success -> {
                    _profileState.value = ProfileState.Success("Profil zapisany!")
                    _navigationEvent.emit(ProfileNavigationEvent.NavigateToMain)
                }
                is Response.Error -> _profileState.value = ProfileState.Error(saveResponse.message)
                else -> {}
            }
        }
    }
}

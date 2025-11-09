package com.example.snookerstats.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SetupProfileState {
    object Idle : SetupProfileState()
    object Loading : SetupProfileState()
    data class Error(val message: String) : SetupProfileState()
}

// Nowy stan do walidacji nazwy użytkownika
data class UsernameValidationState(
    val isChecking: Boolean = false,
    val isAvailable: Boolean? = null // null = nie sprawdzono, true = dostępna, false = zajęta
)

sealed class ProfileNavigationEvent {
    object NavigateToMain : ProfileNavigationEvent()
    data class NavigateToChat(val chatId: String, val otherUserName: String) : ProfileNavigationEvent()
}


@HiltViewModel
class SetupProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _setupState = MutableStateFlow<SetupProfileState>(SetupProfileState.Idle)
    val setupState = _setupState.asStateFlow()

    private val _usernameValidationState = MutableStateFlow(UsernameValidationState())
    val usernameValidationState = _usernameValidationState.asStateFlow()

    private val _navigationEvent = Channel<ProfileNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()
    
    private var usernameCheckJob: Job? = null

    fun onUsernameChange(username: String) {
        usernameCheckJob?.cancel()
        if (username.isBlank() || username.contains(" ")) {
            _usernameValidationState.value = UsernameValidationState(isAvailable = false)
            return
        }
        _usernameValidationState.value = UsernameValidationState(isChecking = true)
        usernameCheckJob = viewModelScope.launch {
            delay(500L) // Debouncing
            when(val response = repository.isUsernameTaken(username)) {
                is Response.Success -> {
                    _usernameValidationState.value = UsernameValidationState(isAvailable = !response.data)
                }
                is Response.Error -> {
                    // W przypadku błędu na razie uznajemy, że nie jest dostępna
                    _usernameValidationState.value = UsernameValidationState(isAvailable = false)
                }
                else -> {}
            }
        }
    }

    fun saveUserProfile(user: User) {
        viewModelScope.launch {
            if (_usernameValidationState.value.isAvailable != true) return@launch // Dodatkowe zabezpieczenie

            _setupState.value = SetupProfileState.Loading
            when (val response = repository.saveUserProfile(user)) {
                is Response.Success -> {
                    _navigationEvent.send(ProfileNavigationEvent.NavigateToMain)
                }
                is Response.Error -> {
                    _setupState.value = SetupProfileState.Error(response.message)
                }
                else -> {
                    _setupState.value = SetupProfileState.Idle
                }
            }
        }
    }
}

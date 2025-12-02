package com.example.snookerstats.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.ProfileRepository
import com.example.snookerstats.util.Resource
import com.google.firebase.auth.FirebaseAuth
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

data class UsernameValidationState(
    val isChecking: Boolean = false,
    val isAvailable: Boolean? = null
)

@HiltViewModel
class SetupProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val firebaseAuth: FirebaseAuth
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
            delay(500L)
            when(val response = repository.isUsernameTaken(username)) {
                is Resource.Success -> {
                    _usernameValidationState.value = UsernameValidationState(isAvailable = !response.data)
                }
                is Resource.Error -> {
                    _usernameValidationState.value = UsernameValidationState(isAvailable = false)
                }
                else -> {}
            }
        }
    }

    fun saveUserProfile(
        username: String,
        firstName: String,
        lastName: String,
        isPublicProfile: Boolean
    ) {
        viewModelScope.launch {
            if (_usernameValidationState.value.isAvailable != true) return@launch

            _setupState.value = SetupProfileState.Loading

            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                _setupState.value = SetupProfileState.Error("Błąd: Użytkownik nie jest zalogowany.")
                return@launch
            }

            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                username = username,
                username_lowercase = username.lowercase(),
                firstName = firstName,
                firstName_lowercase = firstName.lowercase(),
                lastName = lastName,
                lastName_lowercase = lastName.lowercase(),
                publicProfile = isPublicProfile,
                friends = emptyList(),
                friendRequestsSent = emptyList(),
                friendRequestsReceived = emptyList()
            )

            when (val response = repository.saveUserProfile(user)) {
                is Resource.Success -> {
                    _navigationEvent.send(ProfileNavigationEvent.NavigateToMain)
                }
                is Resource.Error -> {
                    _setupState.value = SetupProfileState.Error(response.message)
                }
                else -> {
                    _setupState.value = SetupProfileState.Idle
                }
            }
        }
    }
}

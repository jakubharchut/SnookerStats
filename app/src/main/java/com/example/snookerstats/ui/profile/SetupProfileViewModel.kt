package com.example.snookerstats.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
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

sealed class ProfileNavigationEvent {
    object NavigateToMain : ProfileNavigationEvent()
}

@HiltViewModel
class SetupProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<SetupProfileState>(SetupProfileState.Idle)
    val profileState = _profileState.asStateFlow()

    private val _navigationEvent = Channel<ProfileNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun saveUserProfile(user: User) {
        viewModelScope.launch {
            _profileState.value = SetupProfileState.Loading
            when (val response = repository.saveUserProfile(user)) {
                is Response.Success -> {
                    _navigationEvent.send(ProfileNavigationEvent.NavigateToMain)
                }
                is Response.Error -> {
                    _profileState.value = SetupProfileState.Error(response.message)
                }
                else -> {
                    _profileState.value = SetupProfileState.Idle // Should not happen
                }
            }
        }
    }
}

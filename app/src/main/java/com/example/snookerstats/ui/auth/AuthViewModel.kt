package com.example.snookerstats.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.use_case.ValidateRegisterInputUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class NavigationEvent {
    object NavigateToMain : NavigationEvent()
    object NavigateToRegistrationSuccess : NavigationEvent()
    object NavigateToLogin : NavigationEvent()
    object NavigateToSetupProfile : NavigationEvent()
}

data class CredentialsState(val email: String = "", val password: String = "")

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val validateRegisterInput: ValidateRegisterInputUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent

    private val _credentialsState = MutableStateFlow(CredentialsState())
    val credentialsState: StateFlow<CredentialsState> = _credentialsState.asStateFlow()
    
    init {
        loadCredentialsForPreFill()
    }

    private fun loadCredentialsForPreFill() {
        val credentials = repo.getSavedCredentials()
        if (credentials != null) {
            _credentialsState.value = CredentialsState(email = credentials.first, password = credentials.second)
        }
    }

    fun registerUser(email: String, password: String, confirmPassword: String) {
        // ... (bez zmian)
    }

    fun loginUser(email: String, password: String, rememberMe: Boolean) {
        // ... (bez zmian)
    }

    fun saveUserProfile(username: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            if (username.isBlank()) {
                _authState.value = AuthState.Error("Nazwa wyświetlana jest obowiązkowa.")
                return@launch
            }
            _authState.value = AuthState.Loading
            when (val response = repo.updateUserProfile(username, firstName, lastName)) {
                is Response.Success -> _navigationEvent.emit(NavigationEvent.NavigateToMain)
                is Response.Error -> _authState.value = AuthState.Error(mapFirebaseError(response.message))
                else -> _authState.value = AuthState.Error("Wystąpił nieznany błąd.")
            }
        }
    }

    fun signOut() {
        // ... (bez zmian)
    }

    private fun mapFirebaseError(errorCode: String): String {
        // ... (bez zmian)
    }
}

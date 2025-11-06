package com.example.snookerstats.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.repository.AuthRepository
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
    private val repo: AuthRepository
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
        viewModelScope.launch {
            // Walidacja po stronie klienta (bardzo podstawowa)
            if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                _authState.value = AuthState.Error("Wszystkie pola muszą być wypełnione.")
                return@launch
            }
            if (password != confirmPassword) {
                _authState.value = AuthState.Error("Hasła nie są zgodne.")
                return@launch
            }

            _authState.value = AuthState.Loading
            when (val repoResponse = repo.registerUser(email.trim(), password.trim())) {
                is Response.Success -> _navigationEvent.emit(NavigationEvent.NavigateToRegistrationSuccess)
                is Response.Error -> _authState.value = AuthState.Error(mapFirebaseError(repoResponse.message))
                else -> _authState.value = AuthState.Error("Wystąpił nieznany błąd podczas rejestracji.")
            }
        }
    }

    fun loginUser(email: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            val trimmedEmail = email.trim()
            val trimmedPassword = password.trim()

            if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
                _authState.value = AuthState.Error("Wszystkie pola muszą być wypełnione.")
                return@launch
            }

            _authState.value = AuthState.Loading
            when (val loginResponse = repo.loginUser(trimmedEmail, trimmedPassword)) {
                is Response.Success -> {
                    when (val userDataResponse = repo.getCurrentUserData()) {
                        is Response.Success -> {
                            val user = userDataResponse.data
                            if (user?.username?.isBlank() == true) {
                                if (rememberMe) repo.saveCredentials(trimmedEmail, trimmedPassword) else repo.clearCredentials()
                                _navigationEvent.emit(NavigationEvent.NavigateToSetupProfile)
                            } else {
                                if (rememberMe) repo.saveCredentials(trimmedEmail, trimmedPassword) else repo.clearCredentials()
                                _navigationEvent.emit(NavigationEvent.NavigateToMain)
                            }
                        }
                        is Response.Error -> _authState.value = AuthState.Error(mapFirebaseError(userDataResponse.message))
                        else -> _authState.value = AuthState.Error("Błąd pobierania danych użytkownika.")
                    }
                }
                is Response.Error -> _authState.value = AuthState.Error(mapFirebaseError(loginResponse.message))
                else -> _authState.value = AuthState.Error("Wystąpił nieznany błąd logowania.")
            }
        }
    }

    fun saveUserProfile(username: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            if (username.isBlank()) {
                _authState.value = AuthState.Error("Nazwa wyświetlana jest obowiązkowa.")
                return@launch
            }
            _authState.value = AuthState.Loading

            when (val isTakenResponse = repo.isUsernameTaken(username)) {
                is Response.Success -> {
                    if (isTakenResponse.data) {
                        _authState.value = AuthState.Error("Ta nazwa użytkownika jest już zajęta.")
                        return@launch
                    }
                    when (val updateResponse = repo.updateUserProfile(username, firstName, lastName)) {
                        is Response.Success -> _navigationEvent.emit(NavigationEvent.NavigateToMain)
                        is Response.Error -> _authState.value = AuthState.Error(mapFirebaseError(updateResponse.message))
                        else -> _authState.value = AuthState.Error("Wystąpił nieznany błąd.")
                    }
                }
                is Response.Error -> {
                    _authState.value = AuthState.Error(mapFirebaseError(isTakenResponse.message))
                }
                else -> {}
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repo.signOut()
            _navigationEvent.emit(NavigationEvent.NavigateToLogin)
        }
    }

    private fun mapFirebaseError(errorCode: String): String {
        return when {
            errorCode.contains("EMAIL_EXISTS") || errorCode.contains("email-already-in-use") -> "Ten adres e-mail jest już zajęty."
            errorCode.contains("NETWORK_ERROR") -> "Błąd sieci. Sprawdź połączenie z internetem."
            errorCode.contains("INVALID_CREDENTIAL") || errorCode.contains("wrong-password") -> "Nieprawidłowy e-mail lub hasło."
            errorCode.contains("user-not-found") -> "Nie znaleziono użytkownika o podanym adresie e-mail."
            errorCode.contains("badly formatted") -> "Nieprawidłowy format adresu e-mail."
            else -> "Wystąpił nieznany błąd. Spróbuj ponownie."
        }
    }
}

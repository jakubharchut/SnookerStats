package com.example.snookerstats.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.repository.IAuthRepository
import com.example.snookerstats.domain.use_case.ValidateRegisterInputUseCase
import com.example.snookerstats.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class NavigationEvent {
    object NavigateToMain : NavigationEvent()
    object NavigateToSetupProfile : NavigationEvent()
    object NavigateToRegistrationSuccess : NavigationEvent()
    object NavigateToLogin : NavigationEvent()
}

data class CredentialsState(val email: String = "", val password: String = "")

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: IAuthRepository,
    private val validateRegisterInput: ValidateRegisterInputUseCase,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging
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
            val validationResponse = validateRegisterInput(email, password, confirmPassword)
            if (validationResponse is Resource.Error) {
                _authState.value = AuthState.Error(validationResponse.message)
                return@launch
            }

            _authState.value = AuthState.Loading
            when (val repoResponse = repo.registerUser(email.trim(), password.trim())) {
                is Resource.Success -> _navigationEvent.emit(NavigationEvent.NavigateToRegistrationSuccess)
                is Resource.Error -> _authState.value = AuthState.Error(mapFirebaseError(repoResponse.message))
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
            when (val response = repo.loginUser(trimmedEmail, trimmedPassword)) {
                is Resource.Success -> {
                    val user = firebaseAuth.currentUser
                    if (user != null && user.isEmailVerified) {
                        if (rememberMe) {
                            repo.saveCredentials(trimmedEmail, trimmedPassword)
                        }
                        
                        repo.getUserProfile(user.uid).collectLatest { profileResponse ->
                            when (profileResponse) {
                                is Resource.Success -> {
                                    updateFcmToken()
                                    if (profileResponse.data.username.isBlank()) {
                                        _navigationEvent.emit(NavigationEvent.NavigateToSetupProfile)
                                    } else {
                                        _navigationEvent.emit(NavigationEvent.NavigateToMain)
                                    }
                                }
                                is Resource.Error -> _authState.value = AuthState.Error("Nie udało się pobrać profilu.")
                                else -> {}
                            }
                        }
                    } else {
                        _authState.value = AuthState.Error("Konto nie zostało zweryfikowane. Sprawdź e-mail.")
                    }
                }
                is Resource.Error -> _authState.value = AuthState.Error(mapFirebaseError(response.message))
                else -> _authState.value = AuthState.Error("Wystąpił nieznany błąd podczas logowania.")
            }
        }
    }

    private fun updateFcmToken() {
        viewModelScope.launch {
            try {
                val token = firebaseMessaging.token.await()
                Log.d("FCM_TOKEN_DEBUG", "Generated FCM Token: $token")
                repo.updateFcmToken(token)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to get/update FCM token", e)
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

package com.example.snookerstats.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.use_case.ValidateRegisterInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val validateRegisterInput: ValidateRegisterInputUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun registerUser(username: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            val validationResponse = validateRegisterInput(email, password, confirmPassword)
            if (validationResponse is Response.Error) {
                _authState.value = AuthState.Error(validationResponse.message)
                return@launch
            }

            _authState.value = AuthState.Loading
            when(val repoResponse = repo.registerUser(username, email, password)) {
                is Response.Success -> _authState.value = AuthState.Success("Rejestracja pomyślna! Sprawdź e-mail, aby zweryfikować konto.")
                is Response.Error -> _authState.value = AuthState.Error(repoResponse.message)
                else -> {}
            }
        }
    }
}

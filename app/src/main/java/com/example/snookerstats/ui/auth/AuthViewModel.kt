package com.example.snookerstats.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

// Definicja stanu, który będzie obserwowany przez UI
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun registerUser(email: String, password: String, confirmPassword: String) {
        // Walidacja Danych
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _authState.value = AuthState.Error("Wszystkie pola muszą być wypełnione.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Nieprawidłowy format adresu e-mail.")
            return
        }
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Hasła nie są zgodne.")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Hasło musi mieć co najmniej 6 znaków.")
            return
        }

        // Proces Rejestracji
        _authState.value = AuthState.Loading
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sukces - wysyłanie e-maila weryfikacyjnego
                    Firebase.auth.currentUser?.sendEmailVerification()
                        ?.addOnCompleteListener { sendTask ->
                            if (sendTask.isSuccessful) {
                                _authState.value = AuthState.Success("Rejestracja pomyślna! Sprawdź e-mail, aby zweryfikować konto.")
                            } else {
                                _authState.value = AuthState.Error("Nie udało się wysłać e-maila weryfikacyjnego.")
                            }
                        }
                } else {
                    // Błąd
                    val errorMessage = task.exception?.message ?: "Wystąpił nieznany błąd."
                    _authState.value = AuthState.Error(mapFirebaseError(errorMessage))
                }
            }
    }

    fun loginUser(email: String, password: String) {
        // Walidacja
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Wszystkie pola muszą być wypełnione.")
            return
        }

        // Proces Logowania
        _authState.value = AuthState.Loading
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Logowanie pomyślne
                    // TODO: Sprawdzić, czy e-mail jest zweryfikowany
                    _authState.value = AuthState.Success("Zalogowano pomyślnie!")
                } else {
                    // Błąd logowania
                    val errorMessage = task.exception?.message ?: "Wystąpił nieznany błąd."
                    _authState.value = AuthState.Error(mapFirebaseError(errorMessage))
                }
            }
    }

    // Funkcja do mapowania błędów Firebase na bardziej przyjazne komunikaty
    private fun mapFirebaseError(errorCode: String): String {
        return when {
            errorCode.contains("EMAIL_EXISTS") || errorCode.contains("email-already-in-use") -> "Ten adres e-mail jest już zajęty."
            errorCode.contains("NETWORK_ERROR") -> "Błąd sieci. Sprawdź połączenie z internetem."
            errorCode.contains("INVALID_CREDENTIAL") || errorCode.contains("wrong-password") -> "Nieprawidłowy e-mail lub hasło."
            errorCode.contains("user-not-found") -> "Nie znaleziono użytkownika o podanym adresie e-mail."
            else -> "Błąd: $errorCode"
        }
    }
}

package com.example.snookerstats.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageProfileUiState(
    val isPublic: Boolean = true,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ManageProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageProfileUiState())
    val uiState: StateFlow<ManageProfileUiState> = _uiState.asStateFlow()

    private val userId = firebaseAuth.currentUser?.uid ?: ""

    init {
        if (userId.isNotEmpty()) {
            loadAndObserveUserProfile()
        } else {
            _uiState.value = ManageProfileUiState(isLoading = false, error = "Użytkownik nie jest zalogowany.")
        }
    }

    private fun loadAndObserveUserProfile() {
        viewModelScope.launch {
            authRepository.getUserProfile(userId).collect { response ->
                when (response) {
                    is Response.Success -> {
                        _uiState.value = ManageProfileUiState(
                            isPublic = response.data.publicProfile,
                            isLoading = false
                        )
                    }
                    is Response.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = response.message
                        )
                    }
                    is Response.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun updateProfileVisibility(isPublic: Boolean) {
        // Natychmiastowa aktualizacja UI dla płynnego działania przełącznika
        _uiState.value = _uiState.value.copy(isPublic = isPublic)
        
        viewModelScope.launch {
            // Zapis zmiany w tle
            authRepository.updateProfileVisibility(isPublic)
        }
    }
}

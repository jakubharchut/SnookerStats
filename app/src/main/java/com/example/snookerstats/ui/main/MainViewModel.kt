package com.example.snookerstats.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.repository.IAuthRepository
import com.example.snookerstats.util.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _username = MutableStateFlow("Witaj!")
    val username: StateFlow<String> = _username.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                authRepository.getUserProfile(currentUser.uid).collectLatest { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val user = resource.data
                            if (user.username.isNotBlank()) {
                                _username.value = "Witaj, ${user.username}"
                            } else {
                                _username.value = "Witaj, dokończ profil!"
                            }
                        }
                        is Resource.Error -> {
                            _username.value = "Witaj! (Błąd ładowania profilu)"
                        }
                        else -> {}
                    }
                }
            } else {
                _username.value = "Witaj! (Niezalogowany)"
            }
        }
    }
}

package com.example.snookerstats.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _username = MutableStateFlow("Witaj!")
    val username: StateFlow<String> = _username.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val response = authRepository.getCurrentUserData()) {
                is Response.Success -> {
                    val user = response.data
                    if (user != null && user.username.isNotBlank()) {
                        _username.value = "Witaj, ${user.username}"
                    }
                }
                is Response.Error -> {
                    // W przypadku błędu, zostawiamy domyślny tekst
                }
                else -> {}
            }
        }
    }
}

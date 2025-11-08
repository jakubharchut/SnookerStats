package com.example.snookerstats.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Response
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityUiState(
    val searchQuery: String = "",
    val searchResults: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: CommunityRepository
) : ViewModel() {

    var uiState by mutableStateOf(CommunityUiState())
        private set

    private var searchJob: Job? = null

    fun onSearchQueryChange(query: String) {
        uiState = uiState.copy(searchQuery = query)
        searchJob?.cancel() // Anuluj poprzednie zadanie wyszukiwania
        searchJob = viewModelScope.launch {
            delay(500L) // Poczekaj 500ms
            searchUsers()
        }
    }

    fun searchUsers() {
        searchJob?.cancel() // Anuluj opóźnione wyszukiwanie, jeśli użytkownik kliknie przycisk
        viewModelScope.launch {
            val query = uiState.searchQuery
            if (query.length < 3) { // Nie szukaj, jeśli zapytanie jest zbyt krótkie
                uiState = uiState.copy(searchResults = emptyList())
                return@launch
            }
            Log.d("CommunityViewModel", "Rozpoczynanie wyszukiwania dla: '$query'")
            repository.searchUsers(query).collect { response ->
                when (response) {
                    is Response.Loading -> {
                        Log.d("CommunityViewModel", "Stan: Ładowanie...")
                        uiState = uiState.copy(isLoading = true)
                    }
                    is Response.Success -> {
                        Log.d("CommunityViewModel", "Stan: Sukces. Znaleziono użytkowników: ${response.data.size}")
                        uiState = uiState.copy(searchResults = response.data, isLoading = false)
                    }
                    is Response.Error -> {
                        Log.e("CommunityViewModel", "Stan: Błąd - ${response.message}")
                        uiState = uiState.copy(errorMessage = response.message, isLoading = false)
                    }
                }
            }
        }
    }
}

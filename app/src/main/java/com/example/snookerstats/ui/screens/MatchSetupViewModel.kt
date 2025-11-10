package com.example.snookerstats.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MatchType { RANKING, SPARRING }
enum class MatchFormat(val reds: Int) { FIFTEEN(15), TEN(10), SIX(6), THREE(3) }

data class MatchSetupUiState(
    val opponent: Resource<User>? = null,
    val opponentType: OpponentType = OpponentType.LOADING,
    val guestName: String = "",
    val matchType: MatchType = MatchType.SPARRING,
    val matchFormat: MatchFormat = MatchFormat.FIFTEEN
)

sealed class OpponentType {
    object LOADING : OpponentType()
    object SOLO : OpponentType()
    object GUEST : OpponentType()
    data class PLAYER(val user: User) : OpponentType()
}

@HiltViewModel
class MatchSetupViewModel @Inject constructor(
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchSetupUiState())
    val uiState = _uiState.asStateFlow()

    private val opponentId: String = savedStateHandle.get<String>("opponentId")!!

    init {
        loadOpponentDetails()
    }

    private fun loadOpponentDetails() {
        viewModelScope.launch {
            when (opponentId) {
                "solo" -> _uiState.value = _uiState.value.copy(opponentType = OpponentType.SOLO)
                "guest" -> _uiState.value = _uiState.value.copy(opponentType = OpponentType.GUEST)
                else -> {
                    when(val result = userRepository.getUser(opponentId)) {
                        is Resource.Success -> {
                            _uiState.value = _uiState.value.copy(opponentType = OpponentType.PLAYER(result.data))
                        }
                        is Resource.Error -> {
                            // TODO: Handle error
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun onGuestNameChange(name: String) {
        _uiState.value = _uiState.value.copy(guestName = name)
    }

    fun onMatchTypeChange(type: MatchType) {
        _uiState.value = _uiState.value.copy(matchType = type)
    }

    fun onMatchFormatChange(format: MatchFormat) {
        _uiState.value = _uiState.value.copy(matchFormat = format)
    }
}

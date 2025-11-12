package com.example.snookerstats.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.MatchStatus
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.IAuthRepository
import com.example.snookerstats.domain.repository.MatchRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class MatchType { RANKING, SPARRING }
enum class MatchFormat(val reds: Int) { FIFTEEN(15), TEN(10), SIX(6), THREE(3) }

data class MatchSetupUiState(
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
    private val matchRepository: MatchRepository,
    private val authRepository: IAuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchSetupUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = Channel<String>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val opponentId: String? = savedStateHandle.get<String>("opponentId")

    init {
        loadOpponentDetails()
    }

    private fun loadOpponentDetails() {
        viewModelScope.launch {
            when (opponentId) {
                "solo" -> _uiState.value = _uiState.value.copy(opponentType = OpponentType.SOLO)
                "guest" -> _uiState.value = _uiState.value.copy(opponentType = OpponentType.GUEST)
                null -> { /* Handle error or default state */ }
                else -> {
                    when(val result = userRepository.getUser(opponentId)) {
                        is Resource.Success -> {
                            result.data?.let { user ->
                                _uiState.value = _uiState.value.copy(opponentType = OpponentType.PLAYER(user))
                            }
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

    fun onStartMatchClicked() {
        viewModelScope.launch {
            val currentState = uiState.value
            val firebaseUser = authRepository.currentUser
            if (firebaseUser == null) { /* Handle not logged in */ return@launch }

            val userResource = userRepository.getUser(firebaseUser.uid)
            val currentUser = (userResource as? Resource.Success)?.data
            if (currentUser == null) { /* Handle user not in db */ return@launch }

            val player2Id = when (val opponent = currentState.opponentType) {
                is OpponentType.PLAYER -> opponent.user.uid
                is OpponentType.GUEST -> "guest_${currentState.guestName}"
                is OpponentType.SOLO -> null
                is OpponentType.LOADING -> return@launch
            }

            val newMatch = Match(
                id = UUID.randomUUID().toString(),
                player1Id = currentUser.uid,
                player2Id = player2Id,
                date = System.currentTimeMillis(),
                matchType = com.example.snookerstats.domain.model.MatchType.valueOf(currentState.matchType.name),
                numberOfReds = currentState.matchFormat.reds,
                status = MatchStatus.IN_PROGRESS, // ZAWSZE IN_PROGRESS
                frames = emptyList()
            )

            matchRepository.createNewMatch(newMatch)
            _navigationEvent.send("scoring/${newMatch.id}/${newMatch.numberOfReds}")
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

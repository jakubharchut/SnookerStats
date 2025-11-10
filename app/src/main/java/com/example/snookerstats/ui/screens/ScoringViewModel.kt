package com.example.snookerstats.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.SnookerBall
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PlayerState(
    val user: User,
    val score: Int = 0,
    val framesWon: Int = 0
)

data class ScoringState(
    val player1: PlayerState? = null,
    val player2: PlayerState? = null,
    val activePlayerId: String? = null,
    val currentBreak: Int = 0,
    val pointsRemaining: Int = 147,
    val redsRemaining: Int = 15,
    val breakHistory: List<SnookerBall> = emptyList(),
    val timer: String = "00:00",
    val isRedNext: Boolean = true, // Nowa zmienna stanu
    val isLoading: Boolean = true
)

@HiltViewModel
class ScoringViewModel @Inject constructor(
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoringState())
    val uiState: StateFlow<ScoringState> = _uiState.asStateFlow()

    init {
        // Na razie używamy zahardcodowanego ID do testów
        val matchId = "test_match_123"

        // TODO: Usunąć, gdy będziemy ładować prawdziwe dane
        loadMockData()

        matchRepository.getMatchStream(matchId).onEach { match ->
            // TODO: Zmapować dane z 'match' na ScoringState
            // _uiState.update { it.copy(isLoading = false) }
        }.launchIn(viewModelScope)
    }

    private fun loadMockData() {
        val user1 = User(uid = "player1", email = "test1@test.com", username = "koobi", firstName = "Jakub", lastName = "Kowalski")
        val user2 = User(uid = "player2", email = "test2@test.com", username = "Merka", firstName = "Anna", lastName = "Nowak")
        _uiState.value = ScoringState(
            player1 = PlayerState(user = user1, framesWon = 1),
            player2 = PlayerState(user = user2, framesWon = 0),
            activePlayerId = "player1",
            isLoading = false
        )
    }

    fun onBallClicked(ball: SnookerBall) {
        _uiState.update { currentState ->
            // Walidacja ruchu
            if (currentState.isRedNext && ball !is SnookerBall.Red) return@update currentState
            if (!currentState.isRedNext && ball is SnookerBall.Red) return@update currentState

            val activePlayerState = if (currentState.activePlayerId == currentState.player1?.user?.uid) {
                currentState.player1
            } else {
                currentState.player2
            }
            
            if (activePlayerState == null) return@update currentState

            val newScore = activePlayerState.score + ball.points
            val newPlayerState = activePlayerState.copy(score = newScore)

            val newRedsRemaining = if (ball is SnookerBall.Red) currentState.redsRemaining - 1 else currentState.redsRemaining
            val pointsOnTableAfterPot = if (newRedsRemaining > 0) {
                newRedsRemaining * 8 + 27
            } else {
                // TODO: Logika dla ostatniej sekwencji kolorów
                currentState.pointsRemaining - ball.points
            }
            
            currentState.copy(
                player1 = if (currentState.activePlayerId == currentState.player1?.user?.uid) newPlayerState else currentState.player1,
                player2 = if (currentState.activePlayerId == currentState.player2?.user?.uid) newPlayerState else currentState.player2,
                currentBreak = currentState.currentBreak + ball.points,
                breakHistory = currentState.breakHistory + ball,
                redsRemaining = newRedsRemaining,
                pointsRemaining = pointsOnTableAfterPot,
                isRedNext = if (ball is SnookerBall.Red) false else true // Zmiana stanu
            )
        }
    }

    fun onFoulClicked() {
        // TODO: Logika obsługi faulu
    }

    fun onSafetyClicked() {
        // TODO: Logika zagrania odstawnego
    }

    fun onMissClicked() {
        _uiState.update { currentState ->
            val nextPlayerId = if (currentState.activePlayerId == currentState.player1?.user?.uid) {
                currentState.player2?.user?.uid
            } else {
                currentState.player1?.user?.uid
            }
            currentState.copy(
                activePlayerId = nextPlayerId,
                currentBreak = 0,
                breakHistory = emptyList(),
                isRedNext = true // Nowe podejście zawsze zaczyna się od czerwonej
            )
        }
    }

    fun onUndoClicked() {
        // TODO: Logika cofania ostatniego ruchu
    }

    fun onEndFrameClicked() {
        // TODO: Logika zakończenia frejma
    }

    fun onRepeatFrameClicked() {
        // TODO: Logika powtórzenia frejma
    }

    fun onEndMatchClicked() {
        // TODO: Logika zakończenia meczu
    }
}

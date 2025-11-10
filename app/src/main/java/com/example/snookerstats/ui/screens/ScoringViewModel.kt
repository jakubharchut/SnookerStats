package com.example.snookerstats.ui.screens

import androidx.lifecycle.SavedStateHandle
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
    val canPotColor: Boolean = false,
    val nextColorBallOn: SnookerBall? = null,
    val isFreeBall: Boolean = false,
    val showFoulDialog: Boolean = false,
    val isLoading: Boolean = true,
    val isFrameOver: Boolean = false
)

@HiltViewModel
class ScoringViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoringState())
    val uiState: StateFlow<ScoringState> = _uiState.asStateFlow()

    private val stateHistory = mutableListOf<ScoringState>()

    init {
        val matchId = "test_match_123"
        val numberOfReds = savedStateHandle.get<Int>("numberOfReds") ?: 15
        val initialPoints = calculatePointsRemaining(numberOfReds, null)

        _uiState.update {
            it.copy(
                redsRemaining = numberOfReds,
                pointsRemaining = initialPoints
            )
        }

        loadMockData()
        matchRepository.getMatchStream(matchId).onEach { match ->
            // TODO: Map data
        }.launchIn(viewModelScope)
    }

    private fun loadMockData() {
        val user1 = User(uid = "player1", email = "test1@test.com", username = "koobi", firstName = "Jakub", lastName = "Kowalski")
        val user2 = User(uid = "player2", email = "test2@test.com", username = "Merka", firstName = "Anna", lastName = "Nowak")
        _uiState.update {
            it.copy(
                player1 = PlayerState(user = user1, framesWon = 1),
                player2 = PlayerState(user = user2, framesWon = 0),
                activePlayerId = "player1",
                isLoading = false
            )
        }
    }

    private fun calculatePointsRemaining(reds: Int, nextColor: SnookerBall?): Int {
        if (reds > 0) {
            return (reds * 8) + 27
        }
        return when (nextColor) {
            SnookerBall.Yellow -> 27
            SnookerBall.Green -> 25
            SnookerBall.Brown -> 22
            SnookerBall.Blue -> 18
            SnookerBall.Pink -> 13
            SnookerBall.Black -> 7
            else -> 0 // Frame is over or in an intermediate state
        }
    }

    fun onBallClicked(ball: SnookerBall) {
        stateHistory.add(uiState.value)
        _uiState.update { currentState ->
            if (currentState.isFrameOver) return@update currentState

            // --- VALIDATION ---
            val isValid = when {
                currentState.isFreeBall -> ball !is SnookerBall.Red
                currentState.redsRemaining == 0 -> {
                    if (currentState.nextColorBallOn == null) ball !is SnookerBall.Red // Any color after last red
                    else ball == currentState.nextColorBallOn // Correct color in sequence
                }
                else -> { // Reds on table
                    if (currentState.canPotColor) ball !is SnookerBall.Red
                    else ball is SnookerBall.Red
                }
            }
            if (!isValid) {
                stateHistory.removeLast() // remove invalid state change
                return@update currentState
            }


            val pointsToAdd = if (currentState.isFreeBall && currentState.redsRemaining > 0) 1 else ball.points
            val activePlayerState = if (currentState.activePlayerId == currentState.player1?.user?.uid) currentState.player1 else currentState.player2
            if (activePlayerState == null) return@update currentState

            val newScore = activePlayerState.score + pointsToAdd
            val newPlayerState = activePlayerState.copy(score = newScore)
            val newRedsRemaining = if (ball is SnookerBall.Red && !currentState.isFreeBall) currentState.redsRemaining - 1 else currentState.redsRemaining
            val nextBreakHistory = currentState.breakHistory + ball

            var canPotColorNext = false
            var nextColorOn: SnookerBall? = null
            var isFrameOverNext = false

            if (newRedsRemaining > 0) {
                canPotColorNext = (ball is SnookerBall.Red || currentState.isFreeBall)
            } else { // ENDGAME
                canPotColorNext = true
                
                // Case 1: Just potted the last red. Now ANY color is available.
                if (currentState.redsRemaining > 0 && newRedsRemaining == 0) {
                    nextColorOn = null
                }
                // Case 2: In the "any color" phase and just potted a color. Next is Yellow.
                else if (currentState.redsRemaining == 0 && currentState.nextColorBallOn == null) {
                    nextColorOn = SnookerBall.Yellow
                }
                // Case 3: Already in the sequence.
                else {
                    nextColorOn = when (ball) {
                        is SnookerBall.Yellow -> SnookerBall.Green
                        is SnookerBall.Green -> SnookerBall.Brown
                        is SnookerBall.Brown -> SnookerBall.Blue
                        is SnookerBall.Blue -> SnookerBall.Pink
                        is SnookerBall.Pink -> SnookerBall.Black
                        else -> null // Includes Black, which ends the sequence
                    }
                }
                
                if (ball is SnookerBall.Black && nextColorOn == null) {
                    isFrameOverNext = true
                }
            }

            val newPointsRemaining = calculatePointsRemaining(newRedsRemaining, nextColorOn)

            currentState.copy(
                player1 = if (currentState.activePlayerId == currentState.player1?.user?.uid) newPlayerState else currentState.player1,
                player2 = if (currentState.activePlayerId == currentState.player2?.user?.uid) newPlayerState else currentState.player2,
                currentBreak = currentState.currentBreak + pointsToAdd,
                breakHistory = nextBreakHistory,
                redsRemaining = newRedsRemaining,
                pointsRemaining = newPointsRemaining,
                canPotColor = canPotColorNext,
                nextColorBallOn = nextColorOn,
                isFreeBall = false,
                isFrameOver = isFrameOverNext
            )
        }
    }
    
    fun onFoulClicked() { _uiState.update { it.copy(showFoulDialog = true) } }
    fun onDismissFoulDialog() { _uiState.update { it.copy(showFoulDialog = false) } }

    fun onFoulConfirmed(foulPoints: Int, isFreeBall: Boolean, redsPotted: Int) {
        stateHistory.add(uiState.value)
        _uiState.update { currentState ->
            val opponentState = if (currentState.activePlayerId == currentState.player1?.user?.uid) currentState.player2 else currentState.player1
            if (opponentState == null) return@update currentState

            val newOpponentScore = opponentState.score + foulPoints
            val newOpponentState = opponentState.copy(score = newOpponentScore)
            
            val newRedsRemaining = (currentState.redsRemaining - redsPotted).coerceAtLeast(0)
            val nextPlayerId = opponentState.user.uid
            
            val nextColorOn = if (newRedsRemaining == 0) SnookerBall.Yellow else null
            val newPointsRemaining = calculatePointsRemaining(newRedsRemaining, nextColorOn)

            currentState.copy(
                player1 = if (opponentState.user.uid == currentState.player1?.user?.uid) newOpponentState else currentState.player1,
                player2 = if (opponentState.user.uid == currentState.player2?.user?.uid) newOpponentState else currentState.player2,
                showFoulDialog = false,
                isFreeBall = isFreeBall,
                activePlayerId = nextPlayerId,
                currentBreak = 0,
                breakHistory = emptyList(),
                canPotColor = newRedsRemaining == 0,
                redsRemaining = newRedsRemaining,
                pointsRemaining = newPointsRemaining,
                nextColorBallOn = nextColorOn
            )
        }
    }

    fun onSafetyClicked() { endTurn() }
    fun onMissClicked() { endTurn() }

    private fun endTurn() {
        stateHistory.add(uiState.value)
        _uiState.update { currentState ->
            val nextPlayerId = if (currentState.activePlayerId == currentState.player1?.user?.uid) currentState.player2?.user?.uid else currentState.player1?.user?.uid
            val nextColorOn = if(currentState.redsRemaining == 0) currentState.nextColorBallOn ?: SnookerBall.Yellow else null
            currentState.copy(
                activePlayerId = nextPlayerId,
                currentBreak = 0,
                breakHistory = emptyList(),
                canPotColor = false,
                isFreeBall = false,
                nextColorBallOn = nextColorOn
            )
        }
    }

    fun onUndoClicked() {
        if (stateHistory.isNotEmpty()) {
            val lastState = stateHistory.removeLast()
            _uiState.value = lastState
        }
    }
    fun onEndFrameClicked() { /* TODO */ }
    fun onRepeatFrameClicked() { /* TODO */ }
    fun onEndMatchClicked() { /* TODO */ }
}

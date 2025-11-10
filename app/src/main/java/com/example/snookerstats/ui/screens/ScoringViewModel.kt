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
    val isLoading: Boolean = true
)

@HiltViewModel
class ScoringViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoringState())
    val uiState: StateFlow<ScoringState> = _uiState.asStateFlow()

    init {
        val matchId = "test_match_123"
        val numberOfReds = savedStateHandle.get<Int>("numberOfReds") ?: 15
        val initialPoints = (numberOfReds * 8) + 27

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

    fun onBallClicked(ball: SnookerBall) {
        _uiState.update { currentState ->
            if (currentState.redsRemaining == 0 && !currentState.isFreeBall && currentState.nextColorBallOn != null) {
                if (ball != currentState.nextColorBallOn) return@update currentState
            }
            
            var pointsToAdd = ball.points
            if (currentState.isFreeBall) {
                if (ball is SnookerBall.Red) return@update currentState
                pointsToAdd = if (currentState.redsRemaining > 0) 1 else currentState.nextColorBallOn?.points ?: 0
            } else {
                if (!currentState.canPotColor && ball !is SnookerBall.Red) return@update currentState
            }

            val activePlayerState = if (currentState.activePlayerId == currentState.player1?.user?.uid) currentState.player1 else currentState.player2
            if (activePlayerState == null) return@update currentState

            val newScore = activePlayerState.score + pointsToAdd
            val newPlayerState = activePlayerState.copy(score = newScore)

            val newRedsRemaining = if (ball is SnookerBall.Red && !currentState.isFreeBall) currentState.redsRemaining - 1 else currentState.redsRemaining
            
            val canPotColorNext = if (currentState.isFreeBall) true else ball is SnookerBall.Red
            val nextBreakHistory = currentState.breakHistory + ball

            val nextColorOn = if (newRedsRemaining == 0) {
                if (ball is SnookerBall.Red && newRedsRemaining == 0) null 
                else getNextColorBallOn(nextBreakHistory)
            } else null

            currentState.copy(
                player1 = if (currentState.activePlayerId == currentState.player1?.user?.uid) newPlayerState else currentState.player1,
                player2 = if (currentState.activePlayerId == currentState.player2?.user?.uid) newPlayerState else currentState.player2,
                currentBreak = currentState.currentBreak + pointsToAdd,
                breakHistory = nextBreakHistory,
                redsRemaining = newRedsRemaining,
                canPotColor = canPotColorNext || (newRedsRemaining == 0 && nextColorOn == null),
                nextColorBallOn = nextColorOn,
                isFreeBall = false
            )
        }
    }
    
    private fun getNextColorBallOn(breakHistory: List<SnookerBall>): SnookerBall? {
        val pottedColors = breakHistory.filter { it !is SnookerBall.Red }
        return when {
            !pottedColors.any { it is SnookerBall.Yellow } -> SnookerBall.Yellow
            !pottedColors.any { it is SnookerBall.Green } -> SnookerBall.Green
            !pottedColors.any { it is SnookerBall.Brown } -> SnookerBall.Brown
            !pottedColors.any { it is SnookerBall.Blue } -> SnookerBall.Blue
            !pottedColors.any { it is SnookerBall.Pink } -> SnookerBall.Pink
            !pottedColors.any { it is SnookerBall.Black } -> SnookerBall.Black
            else -> null
        }
    }

    fun onFoulClicked() { _uiState.update { it.copy(showFoulDialog = true) } }
    fun onDismissFoulDialog() { _uiState.update { it.copy(showFoulDialog = false) } }

    fun onFoulConfirmed(foulPoints: Int, isFreeBall: Boolean, redsPotted: Int) {
        _uiState.update { currentState ->
            val opponentState = if (currentState.activePlayerId == currentState.player1?.user?.uid) currentState.player2 else currentState.player1
            if (opponentState == null) return@update currentState

            val newOpponentScore = opponentState.score + foulPoints
            val newOpponentState = opponentState.copy(score = newOpponentScore)
            
            val newRedsRemaining = (currentState.redsRemaining - redsPotted).coerceAtLeast(0)
            val nextPlayerId = opponentState.user.uid

            currentState.copy(
                player1 = if (opponentState.user.uid == currentState.player1?.user?.uid) newOpponentState else currentState.player1,
                player2 = if (opponentState.user.uid == currentState.player2?.user?.uid) newOpponentState else currentState.player2,
                showFoulDialog = false,
                isFreeBall = isFreeBall,
                activePlayerId = nextPlayerId,
                currentBreak = 0,
                breakHistory = emptyList(),
                canPotColor = false,
                redsRemaining = newRedsRemaining,
                nextColorBallOn = if (newRedsRemaining == 0) getNextColorBallOn(emptyList()) else null
            )
        }
    }

    fun onSafetyClicked() { endTurn() }
    fun onMissClicked() { endTurn() }

    private fun endTurn() {
        _uiState.update { currentState ->
            val nextPlayerId = if (currentState.activePlayerId == currentState.player1?.user?.uid) currentState.player2?.user?.uid else currentState.player1?.user?.uid
            currentState.copy(
                activePlayerId = nextPlayerId,
                currentBreak = 0,
                breakHistory = emptyList(),
                canPotColor = false,
                isFreeBall = false,
                nextColorBallOn = if(currentState.redsRemaining == 0) getNextColorBallOn(currentState.breakHistory) else null
            )
        }
    }

    fun onUndoClicked() { /* TODO */ }
    fun onEndFrameClicked() { /* TODO */ }
    fun onRepeatFrameClicked() { /* TODO */ }
    fun onEndMatchClicked() { /* TODO */ }
}

package com.example.snookerstats.ui.screens

import androidx.lifecycle.ViewModel
import com.example.snookerstats.domain.model.SnookerBall
import com.example.snookerstats.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class PlayerState(
    val user: User, // Zakładamy, że model User istnieje
    val score: Int = 0,
    val framesWon: Int = 0
)

data class ScoringState(
    val player1: PlayerState? = null,
    val player2: PlayerState? = null,
    val currentBreak: Int = 0,
    val pointsRemaining: Int = 147,
    val redsRemaining: Int = 15,
    val breakHistory: List<SnookerBall> = emptyList(),
    val timer: String = "00:00",
    val isLoading: Boolean = true
)

@HiltViewModel
class ScoringViewModel @Inject constructor(
    // TODO: private val matchRepository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoringState())
    val uiState: StateFlow<ScoringState> = _uiState.asStateFlow()

    init {
        // TODO: Tutaj zainicjujemy ładowanie danych meczu z repozytorium
    }

    fun onBallClicked(ball: SnookerBall) {
        // TODO: Logika dodawania bili do brejka
    }

    fun onFoulClicked() {
        // TODO: Logika obsługi faulu
    }

    fun onSafetyClicked() {
        // TODO: Logika zagrania odstawnego
    }

    fun onMissClicked() {
        // TODO: Logika pudła
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

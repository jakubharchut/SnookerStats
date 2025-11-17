package com.example.snookerstats.ui.screens.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.SnookerBall
import com.example.snookerstats.domain.model.TrainingAttempt
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LineUpTrainingState(
    val redsRemaining: Int = 15,
    val pottingColor: Boolean = false,
    val finalSequenceBall: SnookerBall? = null,
    val score: Int = 0,
    val pottedBalls: List<SnookerBall> = emptyList(),
    val isFinished: Boolean = false,
    val elapsedTimeInSeconds: Long = 0L
) {
    val ballsOnTable: List<SnookerBall>
        get() {
            if (isFinished) return emptyList()

            val colors = listOf(SnookerBall.Yellow, SnookerBall.Green, SnookerBall.Brown, SnookerBall.Blue, SnookerBall.Pink, SnookerBall.Black)
            val onTable = mutableListOf<SnookerBall>()

            if (finalSequenceBall != null) {
                val startIndex = colors.indexOf(finalSequenceBall)
                if (startIndex != -1) {
                    onTable.addAll(colors.subList(startIndex, colors.size))
                }
            } else {
                repeat(redsRemaining) {
                    onTable.add(SnookerBall.Red)
                }
                onTable.addAll(colors)
            }
            return onTable
        }

    val nextBallToPot: SnookerBall?
        get() {
            return when {
                isFinished -> null
                finalSequenceBall != null -> finalSequenceBall
                pottingColor -> null // Any color
                else -> SnookerBall.Red
            }
        }
}

@HiltViewModel
class LineUpViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LineUpTrainingState())
    val uiState: StateFlow<LineUpTrainingState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    private fun startTimer() {
        stopTimer()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(elapsedTimeInSeconds = it.elapsedTimeInSeconds + 1) }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun onTableBallClick(ball: SnookerBall) {
        val currentState = _uiState.value
        if (currentState.isFinished) return

        val wasSuccessful = if (currentState.finalSequenceBall != null) {
            ball == currentState.finalSequenceBall
        } else if (currentState.pottingColor) {
            ball != SnookerBall.Red
        } else {
            ball == SnookerBall.Red
        }

        if (wasSuccessful) {
            if (currentState.pottedBalls.isEmpty()) {
                startTimer()
            }
            handleSuccessfulPot(ball)
        } else {
            onMiss()
        }
    }

    private fun handleSuccessfulPot(ball: SnookerBall) {
        _uiState.update { currentState ->
            val pointsToAdd = when {
                currentState.pottingColor -> ball.points
                currentState.finalSequenceBall != null -> ball.points
                else -> 1 // Red ball
            }
            val newScore = currentState.score + pointsToAdd
            val newPottedBalls = currentState.pottedBalls + ball

            if (currentState.finalSequenceBall != null) {
                val colors = listOf(SnookerBall.Yellow, SnookerBall.Green, SnookerBall.Brown, SnookerBall.Blue, SnookerBall.Pink, SnookerBall.Black)
                val nextIndex = colors.indexOf(currentState.finalSequenceBall) + 1
                val isFinished = nextIndex >= colors.size
                if (isFinished) {
                    stopTimer()
                    saveAttempt(newScore, newPottedBalls, currentState.elapsedTimeInSeconds)
                }
                currentState.copy(
                    score = newScore,
                    pottedBalls = newPottedBalls,
                    finalSequenceBall = colors.getOrNull(nextIndex),
                    isFinished = isFinished
                )
            } else if (currentState.pottingColor) {
                val isStartingFinalSequence = currentState.redsRemaining == 0
                currentState.copy(
                    score = newScore,
                    pottedBalls = newPottedBalls,
                    pottingColor = false,
                    finalSequenceBall = if (isStartingFinalSequence) SnookerBall.Yellow else null
                )
            } else { // Potting a red
                val newRedsRemaining = currentState.redsRemaining - 1
                currentState.copy(
                    score = newScore,
                    pottedBalls = newPottedBalls,
                    redsRemaining = newRedsRemaining,
                    pottingColor = true
                )
            }
        }
    }

    fun onMiss() {
        val currentState = _uiState.value
        if (currentState.pottedBalls.isNotEmpty()) {
            saveAttempt(currentState.score, currentState.pottedBalls, currentState.elapsedTimeInSeconds)
        }
        resetTraining()
    }

    fun resetTraining() {
        stopTimer()
        _uiState.value = LineUpTrainingState()
    }

    private fun saveAttempt(score: Int, pottedBalls: List<SnookerBall>, duration: Long) {
        viewModelScope.launch {
            val attempt = TrainingAttempt(
                userId = authRepository.currentUser!!.uid,
                score = score,
                durationInSeconds = duration,
                pottedBalls = pottedBalls.map { it.name }
            )
            trainingRepository.saveTrainingAttempt(attempt)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}

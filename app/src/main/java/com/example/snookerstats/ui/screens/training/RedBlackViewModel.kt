package com.example.snookerstats.ui.screens.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class RedBlackState(
    // Break stats
    val streak: Int = 0,
    val elapsedTimeInSeconds: Long = 0L,
    val isPottingBlack: Boolean = false,
    // Session stats
    val record: Int = 0,
    val totalPotsOnRed: Int = 0,
    val totalPotsOnBlack: Int = 0,
    val totalMissesOnRed: Int = 0,
    val totalMissesOnBlack: Int = 0
)

@HiltViewModel
class RedBlackViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RedBlackState())
    val uiState: StateFlow<RedBlackState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    private fun startTimer() {
        if (timerJob?.isActive == true) return
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

    fun onBallPotted(isBlack: Boolean) {
        val currentState = _uiState.value

        if (currentState.streak == 0 && currentState.elapsedTimeInSeconds == 0L) {
            startTimer()
        }

        if (isBlack == currentState.isPottingBlack) {
            _uiState.update {
                it.copy(
                    streak = it.streak + 1,
                    isPottingBlack = !it.isPottingBlack,
                    totalPotsOnRed = if (!isBlack) it.totalPotsOnRed + 1 else it.totalPotsOnRed,
                    totalPotsOnBlack = if (isBlack) it.totalPotsOnBlack + 1 else it.totalPotsOnBlack
                )
            }
        } else {
            // Potted the wrong ball, counts as a miss
            onMiss()
        }
    }

    fun onMiss() {
        val currentState = _uiState.value
        if (currentState.streak > 0) {
            saveAttempt(currentState.streak, currentState.elapsedTimeInSeconds)
        }
        stopTimer()

        _uiState.update {
            it.copy(
                record = maxOf(it.record, it.streak),
                streak = 0,
                elapsedTimeInSeconds = 0L,
                isPottingBlack = false,
                totalMissesOnRed = if (!it.isPottingBlack && it.streak > 0) it.totalMissesOnRed + 1 else it.totalMissesOnRed,
                totalMissesOnBlack = if (it.isPottingBlack && it.streak > 0) it.totalMissesOnBlack + 1 else it.totalMissesOnBlack
            )
        }
    }

    private fun saveAttempt(streak: Int, duration: Long) {
        viewModelScope.launch {
            val attempt = TrainingAttempt(
                userId = authRepository.currentUser!!.uid,
                trainingType = "RED_BLACK",
                score = streak, // For this training, score is the streak
                durationInSeconds = duration,
                pottedBalls = emptyList() // Not relevant for this training
            )
            trainingRepository.saveTrainingAttempt(attempt)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Make sure to save the last attempt if the user leaves the screen during a break
        val currentState = _uiState.value
        if (currentState.streak > 0) {
            saveAttempt(currentState.streak, currentState.elapsedTimeInSeconds)
        }
        stopTimer()
    }
}
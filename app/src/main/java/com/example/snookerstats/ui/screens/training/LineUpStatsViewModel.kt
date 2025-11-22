package com.example.snookerstats.ui.screens.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.SnookerBall
import com.example.snookerstats.domain.model.TrainingAttempt
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.repository.TrainingRepository
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class BallStats(val pots: Int = 0, val misses: Int = 0)

data class LineUpGlobalStats(
    val bestScore: Int = 0,
    val averageScore: Double = 0.0,
    val attemptCount: Int = 0,
    val bestTimeInSeconds: Long = 0L,
    val averageAttemptTimeInSeconds: Double = 0.0,
    val averageShotTime: Double = 0.0,
    val ballStats: Map<SnookerBall, BallStats> = emptyMap(),
    val scoreHistory: List<Int> = emptyList()
)

data class LineUpStatsState(
    val attemptsByDate: Map<String, List<TrainingAttempt>> = emptyMap(),
    val globalStats: LineUpGlobalStats = LineUpGlobalStats(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LineUpStatsViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LineUpStatsState())
    val uiState: StateFlow<LineUpStatsState> = _uiState.asStateFlow()

    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("pl"))

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            trainingRepository.getTrainingAttempts(authRepository.currentUser!!.uid, "LINE_UP").collectLatest {
                when (it) {
                    is Resource.Loading -> _uiState.value = LineUpStatsState(isLoading = true)
                    is Resource.Success -> {
                        val attempts = it.data ?: emptyList()
                        val globalStats = calculateGlobalStats(attempts)
                        val groupedAttempts = attempts.groupBy { attempt -> dateFormatter.format(attempt.date!!) }
                        _uiState.value = LineUpStatsState(attemptsByDate = groupedAttempts, globalStats = globalStats)
                    }
                    is Resource.Error -> _uiState.value = LineUpStatsState(error = it.message)
                }
            }
        }
    }

    private fun calculateGlobalStats(attempts: List<TrainingAttempt>): LineUpGlobalStats {
        if (attempts.isEmpty()) return LineUpGlobalStats()

        val totalPottedBalls = attempts.sumOf { it.pottedBalls.size }
        val totalDuration = attempts.sumOf { it.durationInSeconds }

        val ballStats = mutableMapOf<SnookerBall, BallStats>()
        val allBalls = listOf(SnookerBall.Red, SnookerBall.Yellow, SnookerBall.Green, SnookerBall.Brown, SnookerBall.Blue, SnookerBall.Pink, SnookerBall.Black)

        allBalls.forEach { ball ->
            val pots = attempts.sumOf { attempt -> attempt.pottedBalls.count { pottedBallName -> pottedBallName == ball.name } }
            val misses = attempts.sumOf { attempt -> attempt.missedBalls.count { missedBallName -> missedBallName == ball.name } }
            if (pots > 0 || misses > 0) {
                ballStats[ball] = BallStats(pots, misses)
            }
        }

        return LineUpGlobalStats(
            bestScore = attempts.maxOfOrNull { it.score } ?: 0,
            averageScore = attempts.map { it.score }.average(),
            attemptCount = attempts.size,
            bestTimeInSeconds = attempts.minOfOrNull { it.durationInSeconds } ?: 0L,
            averageAttemptTimeInSeconds = attempts.map { it.durationInSeconds }.average(),
            averageShotTime = if (totalPottedBalls > 0) totalDuration.toDouble() / totalPottedBalls else 0.0,
            ballStats = ballStats,
            scoreHistory = attempts.map { it.score } // Add score history for the chart
        )
    }
}

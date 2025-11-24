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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// Dedicated data classes for Red-Black Training Stats
data class RedBlackGlobalStats(
    val bestStreak: Int = 0,
    val averageStreak: Double = 0.0,
    val attemptCount: Int = 0,
    val bestTimeInSeconds: Long = 0L, // This might need more specific calculation if needed
    val ballStats: Map<SnookerBall, BallStats> = emptyMap(),
    val dailyAverageHistory: List<Pair<Date, Double>> = emptyList()
)

data class RedBlackStatsState(
    val attemptsByDate: Map<String, List<TrainingAttempt>> = emptyMap(),
    val globalStats: RedBlackGlobalStats = RedBlackGlobalStats(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: FilterPeriod = FilterPeriod.ALL
)

@HiltViewModel
class RedBlackStatsViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RedBlackStatsState())
    val uiState: StateFlow<RedBlackStatsState> = _uiState.asStateFlow()

    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("pl"))
    private var allAttempts: List<TrainingAttempt> = emptyList()

    init {
        loadStats()
    }

    fun onFilterSelected(period: FilterPeriod) {
        _uiState.update { it.copy(selectedFilter = period) }
        processAttempts(allAttempts)
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            trainingRepository.getTrainingAttempts(authRepository.currentUser!!.uid, "RED_BLACK").collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        allAttempts = resource.data ?: emptyList()
                        processAttempts(allAttempts)
                    }
                    is Resource.Error -> _uiState.update { it.copy(error = resource.message, isLoading = false) }
                }
            }
        }
    }

    private fun processAttempts(attempts: List<TrainingAttempt>) {
        val filteredAttempts = filterAttemptsByPeriod(attempts, _uiState.value.selectedFilter)
        val globalStats = calculateGlobalStats(filteredAttempts)
        val groupedAttempts = filteredAttempts.groupBy { attempt -> dateFormatter.format(attempt.date!!) }
        _uiState.update {
            it.copy(
                isLoading = false,
                attemptsByDate = groupedAttempts,
                globalStats = globalStats
            )
        }
    }

    private fun filterAttemptsByPeriod(attempts: List<TrainingAttempt>, period: FilterPeriod): List<TrainingAttempt> {
        if (period == FilterPeriod.ALL) return attempts

        val calendar = Calendar.getInstance()
        when (period) {
            FilterPeriod.LAST_MONTH -> calendar.add(Calendar.MONTH, -1)
            FilterPeriod.LAST_3_MONTHS -> calendar.add(Calendar.MONTH, -3)
            FilterPeriod.LAST_6_MONTHS -> calendar.add(Calendar.MONTH, -6)
            FilterPeriod.LAST_12_MONTHS -> calendar.add(Calendar.YEAR, -1)
            else -> { /* No-op */ }
        }
        val startDate = calendar.time

        return attempts.filter { it.date?.after(startDate) ?: false }
    }

    private fun calculateGlobalStats(attempts: List<TrainingAttempt>): RedBlackGlobalStats {
        if (attempts.isEmpty()) return RedBlackGlobalStats()

        val ballStats = mutableMapOf<SnookerBall, BallStats>()
        val relevantBalls = listOf(SnookerBall.Red, SnookerBall.Black)

        relevantBalls.forEach { ball ->
            val pots = attempts.sumOf { attempt -> attempt.pottedBalls.count { pottedBallName -> pottedBallName == ball.name } }
            val misses = attempts.sumOf { attempt -> attempt.missedBalls.count { missedBallName -> missedBallName == ball.name } }
            if (pots > 0 || misses > 0) {
                ballStats[ball] = BallStats(pots, misses)
            }
        }

        val dailyAverages = attempts
            .filter { it.date != null }
            .groupBy {
                val cal = Calendar.getInstance()
                cal.time = it.date!!
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.time
            }
            .mapValues { entry ->
                entry.value.map { it.score }.average()
            }
            .toList()
            .sortedBy { it.first }

        return RedBlackGlobalStats(
            bestStreak = attempts.maxOfOrNull { it.score } ?: 0,
            averageStreak = attempts.map { it.score }.average(),
            attemptCount = attempts.size,
            bestTimeInSeconds = attempts.minOfOrNull { it.durationInSeconds } ?: 0L,
            ballStats = ballStats,
            dailyAverageHistory = dailyAverages
        )
    }
}
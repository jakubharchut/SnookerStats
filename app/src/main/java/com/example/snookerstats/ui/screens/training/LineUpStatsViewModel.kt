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

enum class FilterPeriod(val displayName: String) {
    ALL("Wszystko"),
    LAST_MONTH("1 mc"),
    LAST_3_MONTHS("3 mc"),
    LAST_6_MONTHS("6 mc"),
    LAST_12_MONTHS("12 mc")
}

data class BallStats(val pots: Int = 0, val misses: Int = 0)

data class LineUpGlobalStats(
    val bestScore: Int = 0,
    val averageScore: Double = 0.0,
    val attemptCount: Int = 0,
    val bestTimeInSeconds: Long = 0L,
    val averageAttemptTimeInSeconds: Double = 0.0,
    val averageShotTime: Double = 0.0,
    val ballStats: Map<SnookerBall, BallStats> = emptyMap(),
    val dailyAverageHistory: List<Pair<Date, Double>> = emptyList()
)

data class LineUpStatsState(
    val attemptsByDate: Map<String, List<TrainingAttempt>> = emptyMap(),
    val globalStats: LineUpGlobalStats = LineUpGlobalStats(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: FilterPeriod = FilterPeriod.ALL
)

@HiltViewModel
class LineUpStatsViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LineUpStatsState())
    val uiState: StateFlow<LineUpStatsState> = _uiState.asStateFlow()

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
            trainingRepository.getTrainingAttempts(authRepository.currentUser!!.uid, "LINE_UP").collectLatest { resource ->
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

        return LineUpGlobalStats(
            bestScore = attempts.maxOfOrNull { it.score } ?: 0,
            averageScore = attempts.map { it.score }.average(),
            attemptCount = attempts.size,
            bestTimeInSeconds = attempts.minOfOrNull { it.durationInSeconds } ?: 0L,
            averageAttemptTimeInSeconds = attempts.map { it.durationInSeconds }.average(),
            averageShotTime = if (totalPottedBalls > 0) totalDuration.toDouble() / totalPottedBalls else 0.0,
            ballStats = ballStats,
            dailyAverageHistory = dailyAverages
        )
    }
}
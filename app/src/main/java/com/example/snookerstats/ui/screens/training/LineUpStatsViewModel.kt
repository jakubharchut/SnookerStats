package com.example.snookerstats.ui.screens.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class LineUpGlobalStats(
    val bestScore: Int = 0,
    val averageScore: Double = 0.0,
    val attemptCount: Int = 0,
    val bestTimeInSeconds: Long = 0L
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

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            trainingRepository.getTrainingAttempts(authRepository.currentUser!!.uid, "LINE_UP").collectLatest {
                when (it) {
                    is Resource.Loading -> _uiState.value = LineUpStatsState(isLoading = true)
                    is Resource.Success -> {
                        val attempts = it.data
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
        return LineUpGlobalStats(
            bestScore = attempts.maxOfOrNull { it.score } ?: 0,
            averageScore = attempts.map { it.score }.average(),
            attemptCount = attempts.size,
            bestTimeInSeconds = attempts.minOfOrNull { it.durationInSeconds } ?: 0L
        )
    }
}

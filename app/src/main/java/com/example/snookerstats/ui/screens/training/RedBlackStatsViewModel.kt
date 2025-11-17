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

data class RedBlackGlobalStats(
    val bestStreak: Int = 0,
    val averageStreak: Double = 0.0,
    val attemptCount: Int = 0,
    val bestTimeInSeconds: Long = 0L
)

data class RedBlackStatsState(
    val attemptsByDate: Map<String, List<TrainingAttempt>> = emptyMap(),
    val globalStats: RedBlackGlobalStats = RedBlackGlobalStats(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RedBlackStatsViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RedBlackStatsState())
    val uiState: StateFlow<RedBlackStatsState> = _uiState.asStateFlow()

    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("pl"))

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            trainingRepository.getTrainingAttempts(authRepository.currentUser!!.uid, "RED_BLACK").collectLatest {
                when (it) {
                    is Resource.Loading -> _uiState.value = RedBlackStatsState(isLoading = true)
                    is Resource.Success -> {
                        val attempts = it.data
                        val globalStats = calculateGlobalStats(attempts)
                        val groupedAttempts = attempts.groupBy { attempt -> dateFormatter.format(attempt.date!!) }
                        _uiState.value = RedBlackStatsState(attemptsByDate = groupedAttempts, globalStats = globalStats)
                    }
                    is Resource.Error -> _uiState.value = RedBlackStatsState(error = it.message)
                }
            }
        }
    }

    private fun calculateGlobalStats(attempts: List<TrainingAttempt>): RedBlackGlobalStats {
        if (attempts.isEmpty()) return RedBlackGlobalStats()

        return RedBlackGlobalStats(
            bestStreak = attempts.maxOfOrNull { it.score } ?: 0,
            averageStreak = attempts.map { it.score }.average(),
            attemptCount = attempts.size,
            bestTimeInSeconds = attempts.filter { it.score > 0 }.minOfOrNull { it.durationInSeconds / it.score } ?: 0L
        )
    }
}

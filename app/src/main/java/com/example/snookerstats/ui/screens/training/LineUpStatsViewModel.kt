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
import javax.inject.Inject

data class LineUpStatsState(
    val attempts: List<TrainingAttempt> = emptyList(),
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

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            trainingRepository.getTrainingAttempts(authRepository.currentUser!!.uid, "LINE_UP").collectLatest {
                when (it) {
                    is Resource.Loading -> _uiState.value = LineUpStatsState(isLoading = true)
                    is Resource.Success -> _uiState.value = LineUpStatsState(attempts = it.data)
                    is Resource.Error -> _uiState.value = LineUpStatsState(error = it.message)
                }
            }
        }
    }
}

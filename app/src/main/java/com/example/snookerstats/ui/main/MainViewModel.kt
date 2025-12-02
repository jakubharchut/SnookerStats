package com.example.snookerstats.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.MatchResult
import com.example.snookerstats.domain.model.TrainingAttempt
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.repository.ChatRepository
import com.example.snookerstats.domain.repository.MatchRepository
import com.example.snookerstats.domain.repository.TrainingRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import com.example.snookerstats.util.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val matchRepository: MatchRepository,
    private val chatRepository: ChatRepository,
    private val trainingRepository: TrainingRepository,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val userId = authRepository.currentUser!!.uid

    val username: StateFlow<String> = flow {
        val user = (userRepository.getUser(userId) as? Resource.Success)?.data
        emit(user?.username ?: "...")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "...")

    val unreadChatCount: StateFlow<Int> = flow {
        chatRepository.getChats().collect { resource ->
            if (resource is Resource.Success) {
                val count = resource.data?.count { chat ->
                    (chat.unreadCounts[userId] ?: 0) > 0
                } ?: 0
                emit(count)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val ongoingMatch: StateFlow<Match?> = matchRepository.getOngoingMatch()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val lastTrainingAttempt: StateFlow<Resource<TrainingAttempt?>> = flow {
        emit(userId)
    }.flatMapLatest { uId ->
        trainingRepository.getLastTrainingAttempt(uId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    val formState: StateFlow<List<MatchResult>> = matchRepository.getMatches(userId, 5)
        .map { resource ->
            if (resource is Resource.Success) {
                resource.data.map { match ->
                    val userFrameScore = match.frames.count { it.player1Points > it.player2Points && it.frameWinnerId == userId }
                    val opponentFrameScore = match.frames.size - userFrameScore
                    when {
                        userFrameScore > opponentFrameScore -> MatchResult.WIN
                        userFrameScore < opponentFrameScore -> MatchResult.LOSS
                        else -> MatchResult.DRAW
                    }
                }
            } else {
                emptyList()
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun showSnackbar(message: String) {
        viewModelScope.launch {
            snackbarManager.showMessage(message)
        }
    }
}

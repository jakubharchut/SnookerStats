package com.example.snookerstats.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.*
import com.example.snookerstats.domain.repository.MatchRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchDetailsUiState(
    val matchItem: MatchHistoryDisplayItem? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val frameDetails: Map<Int, FrameStats> = emptyMap(),
    val frameHistories: Map<Int, List<FrameShotHistory>> = emptyMap()
)

data class FrameStats(
    val player1HighestBreak: Int,
    val player2HighestBreak: Int,
    val durationMillis: Long
)

data class FrameShotHistory(
    val shot: Shot,
    val player1ScoreAfter: Int,
    val player2ScoreAfter: Int,
    val breakValueAfter: Int,
    val activePlayerId: String
)

@HiltViewModel
class MatchDetailsViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchDetailsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadMatchDetails(matchId: String) {
        viewModelScope.launch {
            _uiState.value = MatchDetailsUiState(isLoading = true)

            matchRepository.getMatchStream(matchId).collect { match ->
                if (match != null) {
                    val player1Res = userRepository.getUser(match.player1Id)
                    val player2Res = match.player2Id?.let { userRepository.getUser(it) }

                    val player1 = (player1Res as? Resource.Success)?.data
                    val player2 = (player2Res as? Resource.Success)?.data

                    val p1FramesWon = match.frames.count { it.player1Points > it.player2Points }
                    val p2FramesWon = match.frames.count { it.player2Points > it.player1Points }

                    val frameDetails = mutableMapOf<Int, FrameStats>()
                    val frameHistories = mutableMapOf<Int, List<FrameShotHistory>>()

                    match.frames.forEach { frame ->
                        frameDetails[frame.frameNumber] = calculateFrameStats(frame, match.player1Id, match.player2Id)
                        frameHistories[frame.frameNumber] = generateFrameHistory(frame, match.player1Id, match.player2Id)
                    }

                    val displayItem = MatchHistoryDisplayItem(
                        match = match,
                        player1 = player1,
                        player2 = player2,
                        p1FramesWon = p1FramesWon,
                        p2FramesWon = p2FramesWon
                    )
                    _uiState.value = MatchDetailsUiState(
                        matchItem = displayItem,
                        isLoading = false,
                        frameDetails = frameDetails,
                        frameHistories = frameHistories
                    )
                } else {
                    _uiState.value = MatchDetailsUiState(isLoading = false, error = "Nie znaleziono meczu")
                }
            }
        }
    }

    private fun generateFrameHistory(frame: Frame, player1Id: String, player2Id: String?): List<FrameShotHistory> {
        val history = mutableListOf<FrameShotHistory>()
        var p1Score = 0
        var p2Score = 0
        var currentBreak = 0
        var activePlayerId = player1Id

        for (shot in frame.shots) {
            when (shot.type) {
                ShotType.POTTED, ShotType.FREE_BALL_POTTED_AS_RED, ShotType.FREE_BALL_POTTED_AS_COLOR -> {
                    if (activePlayerId == player1Id) p1Score += shot.points else p2Score += shot.points
                    currentBreak += shot.points
                }
                ShotType.FOUL -> {
                    if (activePlayerId == player1Id) p2Score += shot.points else p1Score += shot.points
                    currentBreak = 0
                }
                ShotType.SAFETY, ShotType.MISS -> {
                    currentBreak = 0
                }
            }

            history.add(FrameShotHistory(shot, p1Score, p2Score, currentBreak, activePlayerId))

            // Zmiana gracza po faulu lub chybieniu
            if (shot.type == ShotType.FOUL || shot.type == ShotType.SAFETY || shot.type == ShotType.MISS) {
                activePlayerId = if (activePlayerId == player1Id) player2Id ?: "" else player1Id
            }
        }
        return history
    }

    private fun calculateFrameStats(frame: Frame, player1Id: String, player2Id: String?): FrameStats {
        var p1Highest = 0
        var p2Highest = 0
        var currentBreak = 0
        var activePlayerId = player1Id

        for (shot in frame.shots) {
            when (shot.type) {
                ShotType.POTTED, ShotType.FREE_BALL_POTTED_AS_RED, ShotType.FREE_BALL_POTTED_AS_COLOR -> {
                    currentBreak += shot.points
                }
                ShotType.FOUL, ShotType.SAFETY, ShotType.MISS -> {
                    if (activePlayerId == player1Id) p1Highest = maxOf(p1Highest, currentBreak)
                    else p2Highest = maxOf(p2Highest, currentBreak)
                    currentBreak = 0
                    activePlayerId = if (activePlayerId == player1Id) player2Id ?: "" else player1Id
                }
            }
        }
        if (activePlayerId == player1Id) p1Highest = maxOf(p1Highest, currentBreak)
        else p2Highest = maxOf(p2Highest, currentBreak)

        val duration = if (frame.shots.isNotEmpty()) frame.shots.last().timestamp - frame.shots.first().timestamp else 0

        return FrameStats(p1Highest, p2Highest, duration)
    }
}

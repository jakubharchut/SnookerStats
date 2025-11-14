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
    val frameDetails: Map<Int, AggregatedStats> = emptyMap(),
    val frameHistories: Map<Int, List<FrameShotHistory>> = emptyMap(),
    val matchStats: AggregatedStats? = null
)

data class AggregatedStats(
    val player1HighestBreak: Int,
    val player2HighestBreak: Int,
    val player1TotalPoints: Int,
    val player2TotalPoints: Int,
    val player1Fouls: Int,
    val player2Fouls: Int,
    val player1Breaks: List<Int>,
    val player2Breaks: List<Int>,
    val durationMillis: Long
) {
    val player1AverageBreak: Double get() = if (player1Breaks.isNotEmpty()) player1Breaks.average() else 0.0
    val player2AverageBreak: Double get() = if (player2Breaks.isNotEmpty()) player2Breaks.average() else 0.0
}

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
                    val p1Resource = userRepository.getUser(match.player1Id)
                    val player1 = if (p1Resource is Resource.Success) p1Resource.data else null

                    val player2: User? = if (match.player2Id?.startsWith("guest_") == true) {
                        val guestName = match.player2Id.removePrefix("guest_")
                        User(uid = match.player2Id, username = guestName, firstName = guestName, lastName = "")
                    } else {
                        match.player2Id?.let {
                            val p2Resource = userRepository.getUser(it)
                            if (p2Resource is Resource.Success) p2Resource.data else null
                        }
                    }

                    if (player1 != null) {
                        val p1FramesWon = match.frames.count { it.player1Points > it.player2Points }
                        val p2FramesWon = match.frames.count { it.player2Points > it.player1Points }

                        val frameDetails = mutableMapOf<Int, AggregatedStats>()
                        val frameHistories = mutableMapOf<Int, List<FrameShotHistory>>()

                        match.frames.forEach { frame ->
                            frameDetails[frame.frameNumber] = calculateFrameStats(frame, match.player1Id, match.player2Id)
                            frameHistories[frame.frameNumber] = generateFrameHistory(frame, match.player1Id, match.player2Id)
                        }

                        val matchStats = calculateMatchStats(frameDetails)

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
                            frameHistories = frameHistories,
                            matchStats = matchStats
                        )
                    } else {
                        _uiState.value = MatchDetailsUiState(isLoading = false, error = "Nie znaleziono gracza 1")
                    }
                } else {
                    _uiState.value = MatchDetailsUiState(isLoading = false, error = "Nie znaleziono meczu")
                }
            }
        }
    }
    
    private fun calculateMatchStats(frameDetails: Map<Int, AggregatedStats>): AggregatedStats {
        val allStats = frameDetails.values
        return AggregatedStats(
            player1HighestBreak = allStats.maxOfOrNull { it.player1HighestBreak } ?: 0,
            player2HighestBreak = allStats.maxOfOrNull { it.player2HighestBreak } ?: 0,
            player1TotalPoints = allStats.sumOf { it.player1TotalPoints },
            player2TotalPoints = allStats.sumOf { it.player2TotalPoints },
            player1Fouls = allStats.sumOf { it.player1Fouls },
            player2Fouls = allStats.sumOf { it.player2Fouls },
            player1Breaks = allStats.flatMap { it.player1Breaks },
            player2Breaks = allStats.flatMap { it.player2Breaks },
            durationMillis = allStats.sumOf { it.durationMillis }
        )
    }

    private fun generateFrameHistory(frame: Frame, player1Id: String, player2Id: String?): List<FrameShotHistory> {
        val history = mutableListOf<FrameShotHistory>()
        var p1Score = 0
        var p2Score = 0
        var currentBreak = 0
        var activePlayerId = player1Id

        for (shot in frame.shots) {
            when (shot.type) {
                ShotType.POTTED, ShotType.FREE_BALL_POTTED -> {
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

            if (shot.type == ShotType.FOUL || shot.type == ShotType.SAFETY || shot.type == ShotType.MISS) {
                activePlayerId = if (activePlayerId == player1Id) player2Id ?: "" else player1Id
            }
        }
        return history
    }

    private fun calculateFrameStats(frame: Frame, player1Id: String, player2Id: String?): AggregatedStats {
        var p1Highest = 0
        var p2Highest = 0
        var p1Fouls = 0
        var p2Fouls = 0
        val p1Breaks = mutableListOf<Int>()
        val p2Breaks = mutableListOf<Int>()

        var currentBreak = 0
        var activePlayerId = player1Id

        for (shot in frame.shots) {
            when (shot.type) {
                ShotType.POTTED, ShotType.FREE_BALL_POTTED -> {
                    currentBreak += shot.points
                }
                ShotType.FOUL -> {
                    if (activePlayerId == player1Id) {
                        p1Highest = maxOf(p1Highest, currentBreak)
                        if(currentBreak > 0) p1Breaks.add(currentBreak)
                        p2Fouls++
                    } else {
                        p2Highest = maxOf(p2Highest, currentBreak)
                        if(currentBreak > 0) p2Breaks.add(currentBreak)
                        p1Fouls++
                    }
                    currentBreak = 0
                    activePlayerId = if (activePlayerId == player1Id) player2Id ?: "" else player1Id
                }
                ShotType.SAFETY, ShotType.MISS -> {
                    if (activePlayerId == player1Id) {
                        p1Highest = maxOf(p1Highest, currentBreak)
                        if(currentBreak > 0) p1Breaks.add(currentBreak)
                    } else {
                        p2Highest = maxOf(p2Highest, currentBreak)
                        if(currentBreak > 0) p2Breaks.add(currentBreak)
                    }
                    currentBreak = 0
                    activePlayerId = if (activePlayerId == player1Id) player2Id ?: "" else player1Id
                }
            }
        }
        if (activePlayerId == player1Id) {
            p1Highest = maxOf(p1Highest, currentBreak)
            if(currentBreak > 0) p1Breaks.add(currentBreak)
        } else {
            p2Highest = maxOf(p2Highest, currentBreak)
            if(currentBreak > 0) p2Breaks.add(currentBreak)
        }

        val duration = if (frame.shots.isNotEmpty()) frame.shots.last().timestamp - frame.shots.first().timestamp else 0

        return AggregatedStats(
            player1HighestBreak = p1Highest,
            player2HighestBreak = p2Highest,
            player1TotalPoints = frame.player1Points,
            player2TotalPoints = frame.player2Points,
            player1Fouls = p1Fouls,
            player2Fouls = p2Fouls,
            player1Breaks = p1Breaks,
            player2Breaks = p2Breaks,
            durationMillis = duration
        )
    }
}
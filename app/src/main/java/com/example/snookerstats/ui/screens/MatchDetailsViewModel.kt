package com.example.snookerstats.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.*
import com.example.snookerstats.domain.repository.ChatRepository
import com.example.snookerstats.domain.repository.MatchRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import com.example.snookerstats.util.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchDetailsUiState(
    val matchItem: MatchHistoryDisplayItem? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val frameDetails: Map<Int, AggregatedStats> = emptyMap(),
    val frameHistories: Map<Int, List<FrameShotHistory>> = emptyMap(),
    val matchStats: AggregatedStats? = null,
    val friends: List<User> = emptyList(),
    val showShareDialog: Boolean = false
)

data class Break(
    val value: Int,
    val balls: List<SnookerBall>,
    val frameNumber: Int
)

data class AggregatedStats(
    val player1HighestBreak: Int,
    val player2HighestBreak: Int,
    val player1TotalPoints: Int,
    val player2TotalPoints: Int,
    val player1Fouls: Int,
    val player2Fouls: Int,
    val player1FoulPointsGiven: Int,
    val player2FoulPointsGiven: Int,
    val player1Breaks: List<Break>,
    val player2Breaks: List<Break>,
    val durationMillis: Long,
    val player1Pots: Int,
    val player2Pots: Int,
    val player1Misses: Int,
    val player2Misses: Int,
    val player1Safeties: Int,
    val player2Safeties: Int,
    val player1SafetySuccessCount: Int,
    val player2SafetySuccessCount: Int,
    val player1ShotTotalTime: Long,
    val player2ShotTotalTime: Long,
    val player1ShotCount: Int,
    val player2ShotCount: Int,
    val winnerId: String?
) {
    val player1AverageBreak: Double get() = if (player1Breaks.isNotEmpty()) player1Breaks.map { it.value }.average() else 0.0
    val player2AverageBreak: Double get() = if (player2Breaks.isNotEmpty()) player2Breaks.map { it.value }.average() else 0.0

    val player1PotSuccess: Double get() {
        val totalAttempts = player1Pots + player1Misses
        return if (totalAttempts > 0) (player1Pots.toDouble() / totalAttempts) * 100 else 0.0
    }
    val player2PotSuccess: Double get() {
        val totalAttempts = player2Pots + player2Misses
        return if (totalAttempts > 0) (player2Pots.toDouble() / totalAttempts) * 100 else 0.0
    }

    val player1SafetySuccess: Double get() {
        return if (player1Safeties > 0) (player1SafetySuccessCount.toDouble() / player1Safeties) * 100 else 0.0
    }
    val player2SafetySuccess: Double get() {
        return if (player2Safeties > 0) (player2SafetySuccessCount.toDouble() / player2Safeties) * 100 else 0.0
    }

    val player1AverageShotTime: Double get() = if (player1ShotCount > 0) player1ShotTotalTime.toDouble() / player1ShotCount / 1000.0 else 0.0
    val player2AverageShotTime: Double get() = if (player2ShotCount > 0) player2ShotTotalTime.toDouble() / player2ShotCount / 1000.0 else 0.0
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
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchDetailsUiState())
    val uiState = _uiState.asStateFlow()

    fun onShareClicked() {
        _uiState.value = _uiState.value.copy(showShareDialog = true)
    }

    fun onDismissShareDialog() {
        _uiState.value = _uiState.value.copy(showShareDialog = false)
    }

    fun shareMatchWithFriend(friend: User) {
        viewModelScope.launch {
            val matchItem = _uiState.value.matchItem ?: return@launch
            val matchId = matchItem.match.id
            val description = "${matchItem.player1?.firstName} ${matchItem.player1?.lastName} vs ${matchItem.player2?.firstName} ${matchItem.player2?.lastName} (${matchItem.p1FramesWon} - ${matchItem.p2FramesWon})"

            val createChatResource = chatRepository.createOrGetChat(friend.uid)
            if (createChatResource is Resource.Success) {
                val chatId = createChatResource.data!!
                val sendResource = chatRepository.sendMatchShareMessage(chatId, matchId, description)
                if (sendResource is Resource.Success) {
                    snackbarManager.showMessage("Mecz udostępniony dla ${friend.firstName} ${friend.lastName}")
                } else {
                    snackbarManager.showMessage("Błąd udostępniania meczu")
                }
            }
            onDismissShareDialog()
        }
    }

    fun loadMatchDetails(matchId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val friendsResource = userRepository.getFriends().first { it !is Resource.Loading }
            val friends = (friendsResource as? Resource.Success)?.data ?: emptyList()

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
                        _uiState.value = _uiState.value.copy(
                            matchItem = displayItem,
                            isLoading = false,
                            friends = friends,
                            frameDetails = frameDetails,
                            frameHistories = frameHistories,
                            matchStats = matchStats
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Nie znaleziono gracza 1", friends = friends)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Nie znaleziono meczu", friends = friends)
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
            player1FoulPointsGiven = allStats.sumOf { it.player1FoulPointsGiven },
            player2FoulPointsGiven = allStats.sumOf { it.player2FoulPointsGiven },
            player1Breaks = allStats.flatMap { it.player1Breaks },
            player2Breaks = allStats.flatMap { it.player2Breaks },
            durationMillis = allStats.sumOf { it.durationMillis },
            player1Pots = allStats.sumOf { it.player1Pots },
            player2Pots = allStats.sumOf { it.player2Pots },
            player1Misses = allStats.sumOf { it.player1Misses },
            player2Misses = allStats.sumOf { it.player2Misses },
            player1Safeties = allStats.sumOf { it.player1Safeties },
            player2Safeties = allStats.sumOf { it.player2Safeties },
            player1SafetySuccessCount = allStats.sumOf { it.player1SafetySuccessCount },
            player2SafetySuccessCount = allStats.sumOf { it.player2SafetySuccessCount },
            player1ShotTotalTime = allStats.sumOf { it.player1ShotTotalTime },
            player2ShotTotalTime = allStats.sumOf { it.player2ShotTotalTime },
            player1ShotCount = allStats.sumOf { it.player1ShotCount },
            player2ShotCount = allStats.sumOf { it.player2ShotCount },
            winnerId = null // Winner is a frame-level stat
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
                ShotType.FOUL, ShotType.MISS_PENALTY -> {
                    if (activePlayerId == player1Id) p2Score += shot.points else p1Score += shot.points
                    currentBreak = 0
                }
                ShotType.SAFETY, ShotType.MISS -> {
                    currentBreak = 0
                }
            }

            history.add(FrameShotHistory(shot, p1Score, p2Score, currentBreak, activePlayerId))

            if (shot.type == ShotType.FOUL || shot.type == ShotType.SAFETY || shot.type == ShotType.MISS || shot.type == ShotType.MISS_PENALTY) {
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
        var p1FoulPointsGiven = 0
        var p2FoulPointsGiven = 0
        val p1Breaks = mutableListOf<Break>()
        val p2Breaks = mutableListOf<Break>()
        var p1Pots = 0
        var p2Pots = 0
        var p1Misses = 0
        var p2Misses = 0
        var p1Safeties = 0
        var p2Safeties = 0
        var p1SafetySuccess = 0
        var p2SafetySuccess = 0
        var p1ShotTotalTime = 0L
        var p2ShotTotalTime = 0L
        var p1ShotCount = 0
        var p2ShotCount = 0

        var currentBreakValue = 0
        var currentBreakBalls = mutableListOf<SnookerBall>()
        var activePlayerId = player1Id
        var lastShotTimestamp = frame.shots.firstOrNull()?.timestamp ?: 0

        for ((index, shot) in frame.shots.withIndex()) {
            val shotTime = shot.timestamp - lastShotTimestamp
            if (activePlayerId == player1Id) {
                p1ShotTotalTime += shotTime
                p1ShotCount++
            } else {
                p2ShotTotalTime += shotTime
                p2ShotCount++
            }
            lastShotTimestamp = shot.timestamp

            if (shot.type == ShotType.SAFETY) {
                if (activePlayerId == player1Id) p1Safeties++ else p2Safeties++
                val nextShotIndex = index + 1
                if (nextShotIndex < frame.shots.size) {
                    val nextShot = frame.shots[nextShotIndex]
                    if (nextShot.type != ShotType.POTTED && nextShot.type != ShotType.FREE_BALL_POTTED) {
                        if (activePlayerId == player1Id) p1SafetySuccess++ else p2SafetySuccess++
                    }
                }
            }

            when (shot.type) {
                ShotType.POTTED, ShotType.FREE_BALL_POTTED -> {
                    currentBreakValue += shot.points
                    SnookerBall.fromName(shot.ballName)?.let { currentBreakBalls.add(it) }
                    if (activePlayerId == player1Id) p1Pots++ else p2Pots++
                }
                ShotType.FOUL, ShotType.SAFETY, ShotType.MISS, ShotType.MISS_PENALTY -> {
                    if (shot.type == ShotType.MISS || shot.type == ShotType.MISS_PENALTY) {
                        if (activePlayerId == player1Id) p1Misses++ else p2Misses++
                    }
                    if (shot.type == ShotType.FOUL || shot.type == ShotType.MISS_PENALTY) {
                        if (activePlayerId == player1Id) {
                            p1Fouls++
                            p2FoulPointsGiven += shot.points
                        } else {
                            p2Fouls++
                            p1FoulPointsGiven += shot.points
                        }
                    }

                    if (currentBreakValue > 0) {
                        if (activePlayerId == player1Id) {
                            p1Highest = maxOf(p1Highest, currentBreakValue)
                            p1Breaks.add(Break(currentBreakValue, currentBreakBalls.toList(), frame.frameNumber))
                        } else {
                            p2Highest = maxOf(p2Highest, currentBreakValue)
                            p2Breaks.add(Break(currentBreakValue, currentBreakBalls.toList(), frame.frameNumber))
                        }
                    }
                    currentBreakValue = 0
                    currentBreakBalls.clear()

                    activePlayerId = if (activePlayerId == player1Id) player2Id ?: "" else player1Id
                }
            }
        }

        if (currentBreakValue > 0) {
            if (activePlayerId == player1Id) {
                p1Highest = maxOf(p1Highest, currentBreakValue)
                p1Breaks.add(Break(currentBreakValue, currentBreakBalls.toList(), frame.frameNumber))
            } else {
                p2Highest = maxOf(p2Highest, currentBreakValue)
                p2Breaks.add(Break(currentBreakValue, currentBreakBalls.toList(), frame.frameNumber))
            }
        }

        val winnerId = when {
            frame.player1Points > frame.player2Points -> player1Id
            frame.player2Points > frame.player1Points -> player2Id
            else -> null
        }

        return AggregatedStats(
            player1HighestBreak = p1Highest,
            player2HighestBreak = p2Highest,
            player1TotalPoints = frame.player1Points,
            player2TotalPoints = frame.player2Points,
            player1Fouls = p1Fouls,
            player2Fouls = p2Fouls,
            player1FoulPointsGiven = p1FoulPointsGiven,
            player2FoulPointsGiven = p2FoulPointsGiven,
            player1Breaks = p1Breaks,
            player2Breaks = p2Breaks,
            durationMillis = if (frame.shots.size > 1) frame.shots.last().timestamp - frame.shots.first().timestamp else 0,
            player1Pots = p1Pots,
            player2Pots = p2Pots,
            player1Misses = p1Misses,
            player2Misses = p2Misses,
            player1Safeties = p1Safeties,
            player2Safeties = p2Safeties,
            player1SafetySuccessCount = p1SafetySuccess,
            player2SafetySuccessCount = p2SafetySuccess,
            player1ShotTotalTime = p1ShotTotalTime,
            player2ShotTotalTime = p2ShotTotalTime,
            player1ShotCount = p1ShotCount,
            player2ShotCount = p2ShotCount,
            winnerId = winnerId
        )
    }
}

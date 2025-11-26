package com.example.snookerstats.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.*
import com.example.snookerstats.domain.repository.MatchRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.ui.main.SnackbarManager
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

sealed class ScoringNavEvent {
    object NavigateToMatchHistory : ScoringNavEvent()
}

data class PlayerState(
    val user: User,
    val score: Int = 0,
    val framesWon: Int = 0
)

data class ScoringState(
    val matchId: String? = null,
    val player1: PlayerState? = null,
    val player2: PlayerState? = null,
    val activePlayerId: String? = null,
    val currentBreak: Int = 0,
    val pointsRemaining: Int = 147,
    val redsRemaining: Int = 15,
    val breakHistory: List<SnookerBall> = emptyList(),
    val timer: String = "00:00",
    val canPotColor: Boolean = false,
    val isFreeBall: Boolean = false,
    val nextColorBallOn: SnookerBall? = null,
    val showFoulDialog: Boolean = false,
    val showFreeBallDialog: Boolean = false,
    val showRepeatFrameDialog: Boolean = false,
    val showEndMatchDialog: Boolean = false,
    val showFrameOverDialog: Boolean = false,
    val showAbandonFrameDialog: Boolean = false,
    val frameWinnerName: String? = null,
    val frameEndScore: String? = null,
    val isLoading: Boolean = true,
    val isFrameOver: Boolean = false,
    val currentFrame: Frame? = null,
    val initialReds: Int = 15
)

@HiltViewModel
class ScoringViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository,
    private val snackbarManager: SnackbarManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoringState())
    val uiState: StateFlow<ScoringState> = _uiState.asStateFlow()

    private val _navEvent = Channel<ScoringNavEvent>()
    val navEvent = _navEvent.receiveAsFlow()

    private var timerJob: Job? = null
    private var timeInMillis = 0L
    private var currentMatch: Match? = null

    init {
        val matchId = savedStateHandle.get<String>("matchId")
        val numberOfReds = savedStateHandle.get<Int>("numberOfReds") ?: 15

        if (matchId == null) {
            // TODO: Handle error
        } else {
            _uiState.update { it.copy(matchId = matchId, redsRemaining = numberOfReds, initialReds = numberOfReds) }
            matchRepository.getMatchStream(matchId).onEach { match ->
                if (match != null) {
                    currentMatch = match
                    if (_uiState.value.player1 == null && match.status == MatchStatus.COMPLETED) {
                        _uiState.update { it.copy(isFrameOver = true) }
                    }
                    updateUiWithMatchData(match)
                }
            }.launchIn(viewModelScope)
        }
    }

    private suspend fun updateUiWithMatchData(match: Match) {
        val player1Result = userRepository.getUser(match.player1Id)
        val player1 = (player1Result as? Resource.Success)?.data

        val player2: User? = when {
            match.player2Id == null -> null
            match.player2Id.startsWith("guest_") -> {
                val guestName = match.player2Id.removePrefix("guest_")
                User(uid = match.player2Id, username = guestName, firstName = guestName, lastName = "")
            }
            else -> {
                val player2Result = userRepository.getUser(match.player2Id)
                (player2Result as? Resource.Success)?.data
            }
        }

        if (player1 != null) {
            val currentFrame = match.frames.lastOrNull() ?: Frame(matchId = match.id, frameNumber = 1)
            val completedFrames = if (match.status == MatchStatus.COMPLETED) match.frames else match.frames.dropLast(1)

            val p1FramesWon = completedFrames.count { it.player1Points > it.player2Points && (it.player1Points != 0 || it.player2Points != 0) }
            val p2FramesWon = completedFrames.count { it.player2Points > it.player1Points && (it.player1Points != 0 || it.player2Points != 0) }

            val reconstructedScoringState = reconstructScoringState(
                shots = currentFrame.shots,
                initialReds = match.numberOfReds,
                player1 = player1,
                player2 = player2
            )

            _uiState.update {
                it.copy(
                    player1 = PlayerState(user = player1, score = reconstructedScoringState.player1?.score ?: 0, framesWon = p1FramesWon),
                    player2 = player2?.let { user -> PlayerState(user = user, score = reconstructedScoringState.player2?.score ?: 0, framesWon = p2FramesWon) },
                    activePlayerId = reconstructedScoringState.activePlayerId,
                    isLoading = false,
                    currentFrame = currentFrame,
                    redsRemaining = reconstructedScoringState.redsRemaining,
                    pointsRemaining = reconstructedScoringState.pointsRemaining,
                    currentBreak = reconstructedScoringState.currentBreak,
                    breakHistory = reconstructedScoringState.breakHistory,
                    canPotColor = reconstructedScoringState.canPotColor,
                    isFreeBall = reconstructedScoringState.isFreeBall,
                    nextColorBallOn = reconstructedScoringState.nextColorBallOn,
                    isFrameOver = reconstructedScoringState.isFrameOver,
                    initialReds = match.numberOfReds
                )
            }
        }
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                timeInMillis += 1000
                val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60
                _uiState.update { it.copy(timer = String.format("%02d:%02d", minutes, seconds)) }
            }
        }
    }

    private fun resetTimer() {
        timerJob?.cancel()
        timeInMillis = 0L
        _uiState.update { it.copy(timer = "00:00") }
    }

    private fun calculatePointsRemaining(reds: Int, nextColor: SnookerBall?): Int {
        if (reds > 0) return (reds * 8) + 27
        return when (nextColor) {
            SnookerBall.Yellow -> 27
            SnookerBall.Green -> 25
            SnookerBall.Brown -> 22
            SnookerBall.Blue -> 18
            SnookerBall.Pink -> 13
            SnookerBall.Black -> 7
            else -> 0
        }
    }

    private fun updateMatchInRepository(match: Match) {
        viewModelScope.launch {
            matchRepository.updateMatch(match)
        }
    }

    private fun updateFrameInMatch(match: Match, updatedFrame: Frame) {
        val frameExists = match.frames.any { it.frameNumber == updatedFrame.frameNumber }

        val updatedFrames = if (frameExists) {
            match.frames.map { if (it.frameNumber == updatedFrame.frameNumber) updatedFrame else it }
        } else {
            match.frames + updatedFrame
        }

        updateMatchInRepository(match.copy(frames = updatedFrames))
    }

    fun onBallClicked(ball: SnookerBall) {
        val activePlayerId = _uiState.value.activePlayerId ?: return
        val newState = _uiState.updateAndGet { currentState ->
            if (currentState.isFrameOver) return@updateAndGet currentState

            val isValid = when {
                currentState.redsRemaining == 0 -> if (currentState.nextColorBallOn == null) ball !is SnookerBall.Red else ball == currentState.nextColorBallOn
                else -> {
                    val lastPottedBall = currentState.breakHistory.lastOrNull()
                    if (ball is SnookerBall.Red) {
                        !currentState.canPotColor || lastPottedBall is SnookerBall.Red
                    } else {
                        currentState.canPotColor
                    }
                }
            }
            if (!isValid) return@updateAndGet currentState

            startTimer()
            val activePlayerState = if (currentState.activePlayerId == currentState.player1?.user?.uid) currentState.player1 else currentState.player2
            if (activePlayerState == null) return@updateAndGet currentState

            val newScore = activePlayerState.score + ball.points
            val newPlayerState = activePlayerState.copy(score = newScore)
            val newRedsRemaining = if (ball is SnookerBall.Red) currentState.redsRemaining - 1 else currentState.redsRemaining

            var canPotColorNext = false
            var nextColorOn: SnookerBall? = null
            var frameShouldEnd = false
            var newActivePlayerId = currentState.activePlayerId
            var newCurrentBreak = currentState.currentBreak + ball.points
            var newBreakHistory = currentState.breakHistory + ball

            if (newRedsRemaining > 0) {
                canPotColorNext = (ball is SnookerBall.Red)
            } else { // ENDGAME
                canPotColorNext = true
                if (ball is SnookerBall.Black && currentState.nextColorBallOn == SnookerBall.Black) {
                    val p1FinalScore = if (currentState.activePlayerId == currentState.player1?.user?.uid) newScore else currentState.player1?.score ?: 0
                    val p2FinalScore = if (currentState.activePlayerId == currentState.player2?.user?.uid) newScore else currentState.player2?.score ?: 0

                    if (p1FinalScore == p2FinalScore) {
                        snackbarManager.showMessage("Remis! Dogrywka na czarnej bili.")
                        frameShouldEnd = false
                        nextColorOn = SnookerBall.Black
                        newActivePlayerId = if (currentState.activePlayerId == currentState.player1?.user?.uid) currentState.player2?.user?.uid else currentState.player1?.user?.uid
                        newCurrentBreak = 0
                        newBreakHistory = emptyList()
                    } else {
                        frameShouldEnd = true
                        nextColorOn = null
                    }
                } else {
                    nextColorOn = when {
                        currentState.redsRemaining > 0 && newRedsRemaining == 0 -> null
                        currentState.nextColorBallOn == null -> SnookerBall.Yellow
                        else -> when (ball) {
                            is SnookerBall.Yellow -> SnookerBall.Green
                            is SnookerBall.Green -> SnookerBall.Brown
                            is SnookerBall.Brown -> SnookerBall.Blue
                            is SnookerBall.Blue -> SnookerBall.Pink
                            is SnookerBall.Pink -> SnookerBall.Black
                            else -> null
                        }
                    }
                }
            }

            currentState.copy(
                player1 = if (currentState.activePlayerId == currentState.player1?.user?.uid) newPlayerState else currentState.player1,
                player2 = if (currentState.activePlayerId == currentState.player2?.user?.uid) newPlayerState else currentState.player2,
                currentBreak = newCurrentBreak,
                breakHistory = newBreakHistory,
                redsRemaining = newRedsRemaining,
                pointsRemaining = calculatePointsRemaining(newRedsRemaining, nextColorOn),
                canPotColor = canPotColorNext,
                nextColorBallOn = nextColorOn,
                isFrameOver = frameShouldEnd,
                activePlayerId = newActivePlayerId
            )
        }
        val shot = Shot(timestamp = System.currentTimeMillis(), ballName = ball.name, points = ball.points, type = ShotType.POTTED, playerId = activePlayerId)
        updateFrameWithNewShot(shot)

        if (newState.isFrameOver) {
            showFrameOverDialog()
        }
    }

    fun onFreeBallClicked() { _uiState.update { it.copy(showFreeBallDialog = true) } }
    fun onDismissFreeBallDialog() { _uiState.update { it.copy(showFreeBallDialog = false) } }

    fun onFreeBallConfirmed(ball: SnookerBall, points: Int) {
        val activePlayerId = _uiState.value.activePlayerId ?: return
        startTimer()
        _uiState.update { it.copy(showFreeBallDialog = false) }
        val shot = Shot(
            timestamp = System.currentTimeMillis(),
            ballName = ball.name,
            points = points,
            type = ShotType.FREE_BALL_POTTED,
            playerId = activePlayerId
        )
        updateFrameWithNewShot(shot)
    }

    private fun updateFrameWithNewShot(shot: Shot) {
        val state = _uiState.value
        val frame = state.currentFrame ?: return
        val updatedFrame = frame.copy(
            player1Points = state.player1?.score ?: 0,
            player2Points = state.player2?.score ?: 0,
            shots = frame.shots + shot
        )
        updateFrameInMatch(currentMatch!!, updatedFrame)
    }

    fun onFoulClicked() { _uiState.update { it.copy(showFoulDialog = true) } }
    fun onDismissFoulDialog() { _uiState.update { it.copy(showFoulDialog = false) } }

    fun onFoulConfirmed(foulPoints: Int, redsPotted: Int, isMiss: Boolean) {
        val activePlayerId = _uiState.value.activePlayerId ?: return
        startTimer()

        val shotType = if (isMiss) ShotType.MISS_PENALTY else ShotType.FOUL

        _uiState.update { currentState ->
            val opponentState = if (currentState.activePlayerId == currentState.player1?.user?.uid) currentState.player2 else currentState.player1
            if (opponentState == null) return@update currentState

            val newOpponentScore = opponentState.score + foulPoints
            val newOpponentState = opponentState.copy(score = newOpponentScore)
            val newRedsRemaining = (currentState.redsRemaining - redsPotted).coerceAtLeast(0)

            val nextPlayerId = if (isMiss) {
                currentState.activePlayerId // Keep the same player
            } else {
                opponentState.user.uid // Switch to opponent
            }

            var nextColorOn: SnookerBall? = currentState.nextColorBallOn
            if (newRedsRemaining == 0 && currentState.redsRemaining > 0) {
                nextColorOn = null
            } else if (newRedsRemaining == 0 && currentState.nextColorBallOn == null) {
                nextColorOn = SnookerBall.Yellow
            }

            currentState.copy(
                player1 = if (opponentState.user.uid == currentState.player1?.user?.uid) newOpponentState else currentState.player1,
                player2 = if (opponentState.user.uid == currentState.player2?.user?.uid) newOpponentState else currentState.player2,
                showFoulDialog = false,
                activePlayerId = nextPlayerId,
                currentBreak = 0,
                breakHistory = emptyList(),
                canPotColor = newRedsRemaining == 0,
                isFreeBall = false,
                redsRemaining = newRedsRemaining,
                pointsRemaining = calculatePointsRemaining(newRedsRemaining, nextColorOn),
                nextColorBallOn = nextColorOn
            )
        }
        val shot = Shot(timestamp = System.currentTimeMillis(), points = foulPoints, type = shotType, redsPottedInFoul = redsPotted, playerId = activePlayerId)
        updateFrameWithNewShot(shot)
    }

    private fun endTurn(action: ShotType) {
        val activePlayerId = _uiState.value.activePlayerId ?: return
        startTimer()

        _uiState.update { currentState ->
            val nextPlayerId = if (currentState.activePlayerId == currentState.player1?.user?.uid) currentState.player2?.user?.uid else currentState.player1?.user?.uid
            val nextColorOn = if (currentState.redsRemaining == 0) currentState.nextColorBallOn ?: SnookerBall.Yellow else null
            currentState.copy(
                activePlayerId = nextPlayerId,
                currentBreak = 0,
                breakHistory = emptyList(),
                canPotColor = false,
                isFreeBall = false,
                nextColorBallOn = nextColorOn
            )
        }
        val shot = Shot(timestamp = System.currentTimeMillis(), type = action, playerId = activePlayerId)
        updateFrameWithNewShot(shot)
    }

    fun onSafetyClicked() = endTurn(ShotType.SAFETY)
    fun onMissClicked() = endTurn(ShotType.MISS)

    private fun reconstructScoringState(
        shots: List<Shot>,
        initialReds: Int,
        player1: User,
        player2: User?
    ): ScoringState {
        var tempPlayer1Score = 0
        var tempPlayer2Score = 0
        var tempRedsRemaining = initialReds
        var tempNextColorBallOn: SnookerBall? = null
        var tempCanPotColor = false
        var tempIsFreeBall = false
        var tempActivePlayerId: String? = player1.uid
        var tempCurrentBreak = 0
        val tempBreakHistory = mutableListOf<SnookerBall>()
        var tempIsFrameOver = false

        for (shot in shots) {
            if (tempIsFrameOver) continue // Don't process shots after frame is over
            val pottedBall = SnookerBall.fromName(shot.ballName)
            val activePlayerBeforeShot = tempActivePlayerId

            when (shot.type) {
                ShotType.POTTED -> {
                    if (tempActivePlayerId == player1.uid) tempPlayer1Score += shot.points else tempPlayer2Score += shot.points

                    val redsBeforeShot = tempRedsRemaining
                    if (pottedBall is SnookerBall.Red) tempRedsRemaining--

                    tempCurrentBreak += shot.points
                    if (pottedBall != null) {
                        tempBreakHistory.add(pottedBall)
                    }

                    if (tempRedsRemaining > 0) {
                        tempCanPotColor = (pottedBall is SnookerBall.Red)
                    } else { // ENDGAME
                        tempCanPotColor = true
                        if (pottedBall is SnookerBall.Black && tempNextColorBallOn == SnookerBall.Black) {
                            if (tempPlayer1Score != tempPlayer2Score) {
                                tempIsFrameOver = true
                                tempNextColorBallOn = null
                            } else {
                                tempNextColorBallOn = SnookerBall.Black
                                tempActivePlayerId = if (activePlayerBeforeShot == player1.uid) player2?.uid else player1.uid
                                tempCurrentBreak = 0
                                tempBreakHistory.clear()
                            }
                        } else {
                            tempNextColorBallOn = when {
                                redsBeforeShot > 0 && tempRedsRemaining == 0 -> null
                                tempNextColorBallOn == null -> SnookerBall.Yellow
                                else -> when (pottedBall) {
                                    is SnookerBall.Yellow -> SnookerBall.Green
                                    is SnookerBall.Green -> SnookerBall.Brown
                                    is SnookerBall.Brown -> SnookerBall.Blue
                                    is SnookerBall.Blue -> SnookerBall.Pink
                                    is SnookerBall.Pink -> SnookerBall.Black
                                    else -> null
                                }
                            }
                        }
                    }
                    tempIsFreeBall = false
                }
                ShotType.FREE_BALL_POTTED -> {
                    if (tempActivePlayerId == player1.uid) tempPlayer1Score += shot.points else tempPlayer2Score += shot.points
                    tempCurrentBreak += shot.points
                    if (pottedBall != null) {
                        tempBreakHistory.add(pottedBall)
                    }
                }
                ShotType.FOUL, ShotType.MISS_PENALTY -> {
                    val opponentId = if (tempActivePlayerId == player1.uid) player2?.uid else player1.uid
                    if (opponentId == player1.uid) tempPlayer1Score += shot.points else tempPlayer2Score += shot.points
                    val redsBeforeFoul = tempRedsRemaining
                    tempRedsRemaining = (tempRedsRemaining - shot.redsPottedInFoul).coerceAtLeast(0)

                    if (shot.type == ShotType.FOUL) { // Only switch player on regular foul
                        tempActivePlayerId = opponentId
                    }

                    tempCurrentBreak = 0
                    tempBreakHistory.clear()
                    tempCanPotColor = tempRedsRemaining == 0
                    if (tempRedsRemaining == 0 && redsBeforeFoul > 0) tempNextColorBallOn = null
                    else if (tempRedsRemaining == 0 && tempNextColorBallOn == null) tempNextColorBallOn = SnookerBall.Yellow
                    tempIsFreeBall = false
                }
                ShotType.SAFETY, ShotType.MISS -> {
                    tempActivePlayerId = if (tempActivePlayerId == player1.uid) player2?.uid else player1.uid
                    tempCurrentBreak = 0
                    tempBreakHistory.clear()
                    tempCanPotColor = tempRedsRemaining == 0
                    tempIsFreeBall = false
                    tempNextColorBallOn = if (tempRedsRemaining == 0) tempNextColorBallOn ?: SnookerBall.Yellow else null
                }
            }
        }

        val finalPointsRemaining = calculatePointsRemaining(tempRedsRemaining, tempNextColorBallOn)

        return ScoringState(
            player1 = PlayerState(user = player1, score = tempPlayer1Score),
            player2 = player2?.let { PlayerState(user = it, score = tempPlayer2Score) },
            activePlayerId = tempActivePlayerId,
            currentBreak = tempCurrentBreak,
            redsRemaining = tempRedsRemaining,
            pointsRemaining = finalPointsRemaining,
            breakHistory = tempBreakHistory,
            canPotColor = tempCanPotColor,
            isFreeBall = tempIsFreeBall,
            nextColorBallOn = tempNextColorBallOn,
            isFrameOver = tempIsFrameOver,
            initialReds = initialReds
        )
    }

    fun onUndoClicked() {
        val match = currentMatch ?: return
        val state = uiState.value
        val currentFrame = state.currentFrame ?: return

        if (currentFrame.shots.isEmpty()) {
            snackbarManager.showMessage("Brak ruchów do cofnięcia w bieżącym frejmie.")
            return
        }

        val updatedShots = currentFrame.shots.dropLast(1)

        val reconstructedState = reconstructScoringState(
            shots = updatedShots,
            initialReds = uiState.value.initialReds,
            player1 = state.player1!!.user,
            player2 = state.player2?.user
        )

        val updatedFrame = currentFrame.copy(
            shots = updatedShots,
            player1Points = reconstructedState.player1?.score ?: 0,
            player2Points = reconstructedState.player2?.score ?: 0
        )

        updateFrameInMatch(match, updatedFrame)
    }

    private fun showFrameOverDialog() {
        val state = uiState.value
        val p1 = state.player1 ?: return
        val p2 = state.player2

        val winnerName = if (p1.score > (p2?.score ?: -1)) {
            p1.user.firstName
        } else {
            p2?.user?.firstName
        }
        val scoreText = "${p1.score} - ${p2?.score ?: 0}"

        _uiState.update { it.copy(showFrameOverDialog = true, frameWinnerName = winnerName, frameEndScore = scoreText) }
    }

    fun onDismissFrameOverDialog() {
        _uiState.update { it.copy(showFrameOverDialog = false) }
    }

    fun onReturnToFrameClicked() {
        _uiState.update { it.copy(showFrameOverDialog = false, isFrameOver = false) }
    }

    fun onNextFrameClicked() {
        val match = currentMatch ?: return
        val state = uiState.value
        val currentFrame = state.currentFrame ?: return

        resetTimer()

        val finalizedFrame = currentFrame.copy(
            player1Points = state.player1?.score ?: 0,
            player2Points = state.player2?.score ?: 0
        )
        val finalizedFrames = match.frames.dropLast(1) + finalizedFrame

        val newFrame = Frame(
            matchId = match.id,
            frameNumber = currentFrame.frameNumber + 1
        )

        val updatedMatch = match.copy(frames = finalizedFrames + newFrame)
        updateMatchInRepository(updatedMatch)
        _uiState.update { it.copy(showFrameOverDialog = false) }
    }

    fun onEndFrameClicked() {
        val state = uiState.value
        val p1Score = state.player1?.score ?: 0
        val p2Score = state.player2?.score ?: 0
        val pointsRemaining = state.pointsRemaining

        if (p1Score == 0 && p2Score == 0) {
            _uiState.update { it.copy(showAbandonFrameDialog = true) }
            return
        }

        if (p1Score == p2Score) {
            _uiState.update { it.copy(showAbandonFrameDialog = true) }
            return
        }

        if (abs(p1Score - p2Score) < pointsRemaining) {
            _uiState.update { it.copy(showAbandonFrameDialog = true) }
            return
        }

        _uiState.update { it.copy(isFrameOver = true) }
        showFrameOverDialog()
    }

    fun onDismissAbandonFrameDialog() {
        _uiState.update { it.copy(showAbandonFrameDialog = false) }
    }

    fun onAbandonFrameConfirmed() {
        onRepeatFrameConfirmed()
        _uiState.update { it.copy(showAbandonFrameDialog = false) }
    }

    fun onRepeatFrameClicked() {
        _uiState.update { it.copy(showRepeatFrameDialog = true) }
    }

    fun onDismissRepeatFrameDialog() {
        _uiState.update { it.copy(showRepeatFrameDialog = false) }
    }

    fun onRepeatFrameConfirmed() {
        val match = currentMatch ?: return
        val currentFrame = uiState.value.currentFrame ?: return

        resetTimer()

        val newFrame = currentFrame.copy(
            player1Points = 0,
            player2Points = 0,
            shots = emptyList()
        )

        val updatedFrames = match.frames.dropLast(1) + newFrame
        updateMatchInRepository(match.copy(frames = updatedFrames))
        _uiState.update { it.copy(showRepeatFrameDialog = false) }
    }

    fun onEndMatchClicked() {
        _uiState.update { it.copy(showEndMatchDialog = true) }
    }

    fun onDismissEndMatchDialog() {
        _uiState.update { it.copy(showEndMatchDialog = false) }
    }

    fun onEndMatchConfirmed() {
        viewModelScope.launch {
            val match = currentMatch ?: return@launch
            val state = uiState.value
            val currentFrame = state.currentFrame ?: return@launch
            resetTimer()

            val p1CurrentFrameScore = state.player1?.score ?: 0
            val p2CurrentFrameScore = state.player2?.score ?: 0

            val framesToFinalize = if (p1CurrentFrameScore == 0 && p2CurrentFrameScore == 0) {
                match.frames.dropLast(1)
            } else {
                val finalizedCurrentFrame = currentFrame.copy(
                    player1Points = p1CurrentFrameScore,
                    player2Points = p2CurrentFrameScore
                )
                match.frames.dropLast(1) + finalizedCurrentFrame
            }

            if (framesToFinalize.isEmpty()) {
                snackbarManager.showMessage("Nie można zapisać pustego meczu.")
                _uiState.update { it.copy(showEndMatchDialog = false) }
                return@launch
            }

            val updatedMatch = match.copy(status = MatchStatus.COMPLETED, frames = framesToFinalize)
            updateMatchInRepository(updatedMatch)
            _uiState.update { it.copy(showEndMatchDialog = false, showFrameOverDialog = false) }
            _navEvent.send(ScoringNavEvent.NavigateToMatchHistory)
        }
    }

    fun onAbandonMatchConfirmed() {
        viewModelScope.launch {
            val match = currentMatch ?: return@launch
            resetTimer()
            matchRepository.deleteMatch(match.id)
            _uiState.update { it.copy(showEndMatchDialog = false) }
            _navEvent.send(ScoringNavEvent.NavigateToMatchHistory)
        }
    }
}

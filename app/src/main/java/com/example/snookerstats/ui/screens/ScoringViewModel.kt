package com.example.snookerstats.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.*
import com.example.snookerstats.domain.repository.MatchRepository
import com.example.snookerstats.ui.main.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

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
    val nextColorBallOn: SnookerBall? = null,
    val isFreeBall: Boolean = false,
    val showFoulDialog: Boolean = false,
    val showRepeatFrameDialog: Boolean = false,
    val showEndMatchDialog: Boolean = false,
    val showFrameOverDialog: Boolean = false,
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
    private val snackbarManager: SnackbarManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoringState())
    val uiState: StateFlow<ScoringState> = _uiState.asStateFlow()

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
        val (player1, player2) = matchRepository.getPlayersForMatch(match.player1Id, match.player2Id ?: "")
        if (player1 != null) {
            val currentFrame = match.frames.lastOrNull() ?: Frame(matchId = match.id, frameNumber = 1)
            val completedFrames = if (match.status == MatchStatus.COMPLETED) match.frames else match.frames.dropLast(1)

            val p1FramesWon = completedFrames.count { it.player1Points > it.player2Points }
            val p2FramesWon = completedFrames.count { it.player2Points > it.player1Points }

            val reconstructedScoringState = reconstructScoringState(
                shots = currentFrame.shots,
                initialReds = match.numberOfReds,
                player1Id = match.player1Id,
                player2Id = match.player2Id
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
                    nextColorBallOn = reconstructedScoringState.nextColorBallOn,
                    isFreeBall = reconstructedScoringState.isFreeBall,
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
        if (_uiState.value.isFreeBall) {
            onFreeBallPotted(ball)
            return
        }

        val newState = _uiState.updateAndGet { currentState ->
            if (currentState.isFrameOver) return@updateAndGet currentState

            val isValid = when {
                currentState.redsRemaining == 0 -> if (currentState.nextColorBallOn == null) ball !is SnookerBall.Red else ball == currentState.nextColorBallOn
                else -> if (currentState.canPotColor) ball !is SnookerBall.Red else ball is SnookerBall.Red
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

            if (newRedsRemaining > 0) {
                canPotColorNext = (ball is SnookerBall.Red)
            } else { // ENDGAME
                canPotColorNext = true
                nextColorOn = when {
                    // This is the first color after the last red
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

                if (ball is SnookerBall.Black && currentState.nextColorBallOn == SnookerBall.Black) {
                    frameShouldEnd = true
                }
            }

            currentState.copy(
                player1 = if (currentState.activePlayerId == currentState.player1?.user?.uid) newPlayerState else currentState.player1,
                player2 = if (currentState.activePlayerId == currentState.player2?.user?.uid) newPlayerState else currentState.player2,
                currentBreak = currentState.currentBreak + ball.points,
                breakHistory = currentState.breakHistory + ball,
                redsRemaining = newRedsRemaining,
                pointsRemaining = calculatePointsRemaining(newRedsRemaining, nextColorOn),
                canPotColor = canPotColorNext,
                nextColorBallOn = nextColorOn,
                isFrameOver = frameShouldEnd
            )
        }
        val shot = Shot(timestamp = System.currentTimeMillis(), ballName = ball.name, points = ball.points, type = ShotType.POTTED)
        updateFrameWithNewShot(shot)
        
        if (newState.isFrameOver) {
            showFrameOverDialog()
        }
    }

    private fun onFreeBallPotted(nominatedBall: SnookerBall) {
        if (nominatedBall is SnookerBall.Red) return // Cannot nominate a red ball

        val (pointsToAdd, shotType) = if (_uiState.value.redsRemaining > 0) {
            1 to ShotType.FREE_BALL_POTTED_AS_RED
        } else {
            (_uiState.value.nextColorBallOn?.points ?: 0) to ShotType.FREE_BALL_POTTED_AS_COLOR
        }

        val newState = _uiState.updateAndGet { currentState ->
            val activePlayerState = if (currentState.activePlayerId == currentState.player1?.user?.uid) currentState.player1 else currentState.player2
            if (activePlayerState == null) return@updateAndGet currentState

            val newScore = activePlayerState.score + pointsToAdd
            val newPlayerState = activePlayerState.copy(score = newScore)

            val canPotColorNext: Boolean
            val nextColorOn: SnookerBall?

            if (currentState.redsRemaining > 0) {
                canPotColorNext = true
                nextColorOn = null
            } else {
                canPotColorNext = true
                // After potting a free ball as a color, the player must pot the actual color that was 'on'
                nextColorOn = currentState.nextColorBallOn
            }

            currentState.copy(
                player1 = if (currentState.activePlayerId == currentState.player1?.user?.uid) newPlayerState else currentState.player1,
                player2 = if (currentState.activePlayerId == currentState.player2?.user?.uid) newPlayerState else currentState.player2,
                currentBreak = currentState.currentBreak + pointsToAdd,
                breakHistory = currentState.breakHistory + nominatedBall,
                pointsRemaining = calculatePointsRemaining(currentState.redsRemaining, nextColorOn),
                canPotColor = canPotColorNext,
                nextColorBallOn = nextColorOn,
                isFreeBall = false
            )
        }
        val shot = Shot(timestamp = System.currentTimeMillis(), ballName = nominatedBall.name, points = pointsToAdd, type = shotType)
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

    fun onFoulConfirmed(foulPoints: Int, isFreeBall: Boolean, redsPotted: Int) {
        startTimer()

        _uiState.update { currentState ->
            val opponentState = if (currentState.activePlayerId == currentState.player1?.user?.uid) currentState.player2 else currentState.player1
            if (opponentState == null) return@update currentState

            val newOpponentScore = opponentState.score + foulPoints
            val newOpponentState = opponentState.copy(score = newOpponentScore)
            val newRedsRemaining = (currentState.redsRemaining - redsPotted).coerceAtLeast(0)
            val nextPlayerId = opponentState.user.uid
            
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
                isFreeBall = isFreeBall,
                activePlayerId = nextPlayerId,
                currentBreak = 0,
                breakHistory = emptyList(),
                canPotColor = newRedsRemaining == 0,
                redsRemaining = newRedsRemaining,
                pointsRemaining = calculatePointsRemaining(newRedsRemaining, nextColorOn),
                nextColorBallOn = nextColorOn
            )
        }
        val shot = Shot(timestamp = System.currentTimeMillis(), points = foulPoints, type = ShotType.FOUL, redsPottedInFoul = redsPotted)
        updateFrameWithNewShot(shot)
    }

    private fun endTurn(action: ShotType) {
        startTimer()
        
        _uiState.update { currentState ->
            val nextPlayerId = if (currentState.activePlayerId == currentState.player1?.user?.uid) currentState.player2?.user?.uid else currentState.player1?.user?.uid
            val nextColorOn = if(currentState.redsRemaining == 0) currentState.nextColorBallOn ?: SnookerBall.Yellow else null
            currentState.copy(
                activePlayerId = nextPlayerId,
                currentBreak = 0,
                breakHistory = emptyList(),
                canPotColor = false,
                isFreeBall = false,
                nextColorBallOn = nextColorOn
            )
        }
        val shot = Shot(timestamp = System.currentTimeMillis(), type = action)
        updateFrameWithNewShot(shot)
    }

    fun onSafetyClicked() = endTurn(ShotType.SAFETY)
    fun onMissClicked() = endTurn(ShotType.MISS)

    private fun reconstructScoringState(
        shots: List<Shot>,
        initialReds: Int,
        player1Id: String,
        player2Id: String?
    ): ScoringState {
        var tempPlayer1Score = 0
        var tempPlayer2Score = 0
        var tempRedsRemaining = initialReds
        var tempNextColorBallOn: SnookerBall? = null
        var tempCanPotColor = false
        var tempIsFreeBall = false
        var tempActivePlayerId: String? = player1Id
        var tempCurrentBreak = 0
        val tempBreakHistory = mutableListOf<SnookerBall>()
        var tempIsFrameOver = false

        for (shot in shots) {
            val pottedBall = SnookerBall.fromName(shot.ballName) ?: SnookerBall.Red

            when (shot.type) {
                ShotType.POTTED -> {
                    if (tempActivePlayerId == player1Id) tempPlayer1Score += shot.points else tempPlayer2Score += shot.points
                    
                    val redsBeforeShot = tempRedsRemaining
                    if (pottedBall is SnookerBall.Red) {
                        tempRedsRemaining--
                    }
                    
                    tempCurrentBreak += shot.points
                    tempBreakHistory.add(pottedBall)

                    if (tempRedsRemaining > 0) {
                        tempCanPotColor = (pottedBall is SnookerBall.Red)
                    } else { // ENDGAME
                        if (pottedBall is SnookerBall.Black && tempNextColorBallOn == SnookerBall.Black) {
                            tempIsFrameOver = true
                        }
                        tempCanPotColor = true
                        
                        tempNextColorBallOn = when {
                            // First color after last red
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
                ShotType.FREE_BALL_POTTED_AS_RED -> {
                    if (tempActivePlayerId == player1Id) tempPlayer1Score += shot.points else tempPlayer2Score += shot.points
                    tempCurrentBreak += shot.points
                    tempBreakHistory.add(pottedBall)
                    tempCanPotColor = true
                    tempIsFreeBall = false
                }
                ShotType.FREE_BALL_POTTED_AS_COLOR -> {
                    if (tempActivePlayerId == player1Id) tempPlayer1Score += shot.points else tempPlayer2Score += shot.points
                    tempCurrentBreak += shot.points
                    tempBreakHistory.add(pottedBall)
                    // Do not advance the next color, the player must pot the actual color that was 'on'
                    tempIsFreeBall = false
                }
                ShotType.FOUL -> {
                    val opponentId = if (tempActivePlayerId == player1Id) player2Id else player1Id
                    if (opponentId == player1Id) tempPlayer1Score += shot.points else tempPlayer2Score += shot.points
                    
                    val redsBeforeFoul = tempRedsRemaining
                    tempRedsRemaining = (tempRedsRemaining - shot.redsPottedInFoul).coerceAtLeast(0)
                    tempActivePlayerId = opponentId
                    tempCurrentBreak = 0
                    tempBreakHistory.clear()
                    tempIsFreeBall = true 
                    tempCanPotColor = tempRedsRemaining == 0

                    if (tempRedsRemaining == 0 && redsBeforeFoul > 0) {
                        tempNextColorBallOn = null
                    } else if (tempRedsRemaining == 0 && tempNextColorBallOn == null) {
                        tempNextColorBallOn = SnookerBall.Yellow
                    }
                }
                ShotType.SAFETY, ShotType.MISS -> {
                    tempActivePlayerId = if (tempActivePlayerId == player1Id) player2Id else player1Id
                    tempCurrentBreak = 0
                    tempBreakHistory.clear()
                    tempIsFreeBall = false
                    tempCanPotColor = tempRedsRemaining == 0
                    tempNextColorBallOn = if (tempRedsRemaining == 0) tempNextColorBallOn ?: SnookerBall.Yellow else null
                }
            }
        }

        val finalPointsRemaining = calculatePointsRemaining(tempRedsRemaining, tempNextColorBallOn)

        return ScoringState(
            player1 = PlayerState(user = User(uid=player1Id), score = tempPlayer1Score),
            player2 = player2Id?.let { PlayerState(user = User(uid=it), score = tempPlayer2Score) },
            activePlayerId = tempActivePlayerId,
            currentBreak = tempCurrentBreak,
            redsRemaining = tempRedsRemaining,
            pointsRemaining = finalPointsRemaining,
            breakHistory = tempBreakHistory,
            canPotColor = tempCanPotColor,
            nextColorBallOn = tempNextColorBallOn,
            isFreeBall = tempIsFreeBall,
            isFrameOver = tempIsFrameOver,
            initialReds = initialReds
        )
    }

    fun onUndoClicked() {
        val match = currentMatch ?: return
        val currentFrame = uiState.value.currentFrame ?: return
        
        if (currentFrame.shots.isEmpty()) {
            snackbarManager.showMessage("Brak ruchów do cofnięcia w bieżącym frejmie.")
            return
        }

        val updatedShots = currentFrame.shots.dropLast(1)

        val reconstructedState = reconstructScoringState(
            shots = updatedShots,
            initialReds = uiState.value.initialReds,
            player1Id = match.player1Id,
            player2Id = match.player2Id
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
        val p2 = state.player2 ?: return
        
        val winner = if (p1.score > p2.score) p1 else p2
        val scoreText = "${p1.score} - ${p2.score}"
        
        _uiState.update { it.copy(showFrameOverDialog = true, frameWinnerName = winner.user.firstName, frameEndScore = scoreText) }
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

        // Finalize the current frame with the scores
        val finalizedFrame = currentFrame.copy(
            player1Points = state.player1?.score ?: 0,
            player2Points = state.player2?.score ?: 0
        )
        val finalizedFrames = match.frames.dropLast(1) + finalizedFrame
        
        // Create the new frame
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

        if (p1Score == p2Score) {
            snackbarManager.showMessage("Nie można zakończyć frejma przy remisie.")
            return
        }
        
        _uiState.update { it.copy(isFrameOver = true) }
        showFrameOverDialog()
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
    }
    
    fun onEndMatchClicked() {
        _uiState.update { it.copy(showEndMatchDialog = true) }
    }
    
    fun onDismissEndMatchDialog() {
        _uiState.update { it.copy(showEndMatchDialog = false) }
    }

    fun onEndMatchConfirmed() {
        val match = currentMatch ?: return
        resetTimer()
        
        val updatedMatch = match.copy(status = MatchStatus.COMPLETED)
        updateMatchInRepository(updatedMatch)
        _uiState.update { it.copy(showEndMatchDialog = false, showFrameOverDialog = false) }
    }
}

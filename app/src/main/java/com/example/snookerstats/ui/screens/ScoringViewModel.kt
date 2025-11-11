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
                    // Initial setup for completed matches
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
            val currentFrame = match.frames.lastOrNull() ?: Frame(matchId = match.id, frameNumber = 1, player1Points = 0, player2Points = 0, shots = emptyList())
            val completedFrames = if (match.status == MatchStatus.COMPLETED) match.frames else match.frames.dropLast(1)

            val p1FramesWon = completedFrames.count { it.player1Points > it.player2Points }
            val p2FramesWon = completedFrames.count { it.player2Points > it.player1Points }

            // REKONSTRUKCJA STANU na podstawie currentFrame.shots, aby uzyskać wszystkie pochodne stany UI
            val reconstructedScoringState = reconstructScoringState(
                shots = currentFrame.shots,
                initialReds = match.numberOfReds, // Użyj match.numberOfReds jako początkowej wartości dla frejma
                player1Id = match.player1Id,
                player2Id = match.player2Id
            )

            _uiState.update {
                it.copy(
                    player1 = PlayerState(user = player1, score = currentFrame.player1Points, framesWon = p1FramesWon),
                    player2 = player2?.let { user -> PlayerState(user = user, score = currentFrame.player2Points, framesWon = p2FramesWon) },
                    activePlayerId = reconstructedScoringState.activePlayerId, // Użyj zrekonstruowanego activePlayerId
                    isLoading = false,
                    currentFrame = currentFrame,
                    // Użyj wartości ze zrekonstruowanego stanu
                    redsRemaining = reconstructedScoringState.redsRemaining,
                    pointsRemaining = reconstructedScoringState.pointsRemaining,
                    currentBreak = reconstructedScoringState.currentBreak,
                    breakHistory = reconstructedScoringState.breakHistory,
                    canPotColor = reconstructedScoringState.canPotColor,
                    nextColorBallOn = reconstructedScoringState.nextColorBallOn,
                    isFreeBall = reconstructedScoringState.isFreeBall,
                    isFrameOver = reconstructedScoringState.isFrameOver,
                    initialReds = match.numberOfReds // Upewnij się, że to również pochodzi z match
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
        val updatedFrames = match.frames.dropLast(1) + updatedFrame
        updateMatchInRepository(match.copy(frames = updatedFrames))
    }

    fun onBallClicked(ball: SnookerBall) {
        val newState = _uiState.updateAndGet { currentState ->
            if (currentState.isFrameOver) return@updateAndGet currentState

            val isValid = when {
                currentState.isFreeBall -> ball !is SnookerBall.Red
                currentState.redsRemaining == 0 -> if (currentState.nextColorBallOn == null) ball !is SnookerBall.Red else ball == currentState.nextColorBallOn
                else -> if (currentState.canPotColor) ball !is SnookerBall.Red else ball is SnookerBall.Red
            }
            if (!isValid) {
                return@updateAndGet currentState
            }
            
            startTimer()
            val pointsToAdd = if (currentState.isFreeBall && currentState.redsRemaining > 0) 1 else ball.points
            val activePlayerState = if (currentState.activePlayerId == currentState.player1?.user?.uid) currentState.player1 else currentState.player2
            if (activePlayerState == null) return@updateAndGet currentState

            val newScore = activePlayerState.score + pointsToAdd
            val newPlayerState = activePlayerState.copy(score = newScore)
            val newRedsRemaining = if (ball is SnookerBall.Red && !currentState.isFreeBall) currentState.redsRemaining - 1 else currentState.redsRemaining
            
            var canPotColorNext = false
            var nextColorOn: SnookerBall? = null
            var frameShouldEnd = false

            if (newRedsRemaining > 0) {
                canPotColorNext = (ball is SnookerBall.Red || currentState.isFreeBall)
            } else { // ENDGAME
                canPotColorNext = true
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
                if (ball is SnookerBall.Black && currentState.nextColorBallOn == SnookerBall.Black) {
                    frameShouldEnd = true
                }
            }

            currentState.copy(
                player1 = if (currentState.activePlayerId == currentState.player1?.user?.uid) newPlayerState else currentState.player1,
                player2 = if (currentState.activePlayerId == currentState.player2?.user?.uid) newPlayerState else currentState.player2,
                currentBreak = currentState.currentBreak + pointsToAdd,
                breakHistory = currentState.breakHistory + ball,
                redsRemaining = newRedsRemaining,
                pointsRemaining = calculatePointsRemaining(newRedsRemaining, nextColorOn),
                canPotColor = canPotColorNext,
                nextColorBallOn = nextColorOn,
                isFreeBall = false,
                isFrameOver = frameShouldEnd
            )
        }
        val shot = Shot(timestamp = System.currentTimeMillis(), ballName = ball.name, points = ball.points, type = ShotType.POTTED, redsPottedInFoul = 0)
        val frame = newState.currentFrame ?: return
        val updatedFrame = frame.copy(
            player1Points = newState.player1?.score ?: 0,
            player2Points = newState.player2?.score ?: 0,
            shots = frame.shots + shot
        )
        updateFrameInMatch(currentMatch!!, updatedFrame)
        
        if (newState.isFrameOver) {
            showFrameOverDialog()
        }
    }
    
    fun onFoulClicked() { _uiState.update { it.copy(showFoulDialog = true) } }
    fun onDismissFoulDialog() { _uiState.update { it.copy(showFoulDialog = false) } }

    fun onFoulConfirmed(foulPoints: Int, isFreeBall: Boolean, redsPotted: Int) {
        startTimer()

        val newState = _uiState.updateAndGet { currentState ->
            val opponentState = if (currentState.activePlayerId == currentState.player1?.user?.uid) currentState.player2 else currentState.player1
            if (opponentState == null) return@updateAndGet currentState

            val newOpponentScore = opponentState.score + foulPoints
            val newOpponentState = opponentState.copy(score = newOpponentScore)
            val newRedsRemaining = (currentState.redsRemaining - redsPotted).coerceAtLeast(0)
            val nextPlayerId = opponentState.user.uid
            val nextColorOn = if (newRedsRemaining == 0) SnookerBall.Yellow else null
            
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
        val shot = Shot(timestamp = System.currentTimeMillis(), ballName = SnookerBall.Red.name, points = foulPoints, type = ShotType.FOUL, redsPottedInFoul = redsPotted)
        val frame = newState.currentFrame ?: return
        val updatedFrame = frame.copy(
            player1Points = newState.player1?.score ?: 0,
            player2Points = newState.player2?.score ?: 0,
            shots = frame.shots + shot
        )
        updateFrameInMatch(currentMatch!!, updatedFrame)
    }

    private fun endTurn(action: ShotType) {
        startTimer()
        
        val newState = _uiState.updateAndGet { currentState ->
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
        val shot = Shot(timestamp = System.currentTimeMillis(), ballName = SnookerBall.Red.name, points = 0, type = action, redsPottedInFoul = 0)
        val frame = newState.currentFrame ?: return
        val updatedFrame = frame.copy(
            player1Points = newState.player1?.score ?: 0,
            player2Points = newState.player2?.score ?: 0,
            shots = frame.shots + shot
        )
        updateFrameInMatch(currentMatch!!, updatedFrame)
    }

    fun onSafetyClicked() = endTurn(ShotType.SAFETY)
    fun onMissClicked() = endTurn(ShotType.MISS)

    // Nowa funkcja pomocnicza do rekonstrukcji ScoringState z listy strzałów
    private fun reconstructScoringState(
        shots: List<Shot>,
        initialReds: Int,
        player1Id: String,
        player2Id: String? // Może być null dla gry solo
    ): ScoringState {
        var tempPlayer1Score = 0
        var tempPlayer2Score = 0
        var tempRedsRemaining = initialReds
        var tempNextColorBallOn: SnookerBall? = null
        var tempCanPotColor = false
        var tempIsFreeBall = false
        var tempActivePlayerId: String? = player1Id // Zmieniono na String?
        var tempCurrentBreak = 0
        val tempBreakHistory = mutableListOf<SnookerBall>()
        var tempIsFrameOver = false

        // Musimy śledzić, kto jest aktywnym graczem w danej turze. Zaczynamy od Player1.
        var currentPlayerTurnId: String? = player1Id // Zmieniono na String?

        for (shot in shots) {
            when (shot.type) {
                ShotType.POTTED -> {
                    val pointsToAdd = if (tempIsFreeBall && tempRedsRemaining > 0) 1 else shot.points
                    if (currentPlayerTurnId == player1Id) {
                        tempPlayer1Score += pointsToAdd
                    } else if (currentPlayerTurnId == player2Id) {
                        tempPlayer2Score += pointsToAdd
                    }

                    val pottedBall = SnookerBall.fromName(shot.ballName)
                    if (pottedBall == null) {
                        // TODO: Handle error, unknown ball name. Log and skip for now.
                        println("Error: Unknown ball name encountered during reconstruction: ${shot.ballName}")
                        continue
                    }

                    if (pottedBall is SnookerBall.Red && !tempIsFreeBall) {
                        tempRedsRemaining--
                    }
                    tempCurrentBreak += pointsToAdd
                    tempBreakHistory.add(pottedBall)

                    // Zaktualizuj canPotColor i nextColorBallOn na podstawie bieżącego stanu po strzale
                    if (tempRedsRemaining > 0) {
                        tempCanPotColor = (pottedBall is SnookerBall.Red || tempIsFreeBall)
                    } else { // KOŃCÓWKA GRY
                        tempCanPotColor = true
                    }

                    tempNextColorBallOn = if (tempRedsRemaining > 0 && pottedBall is SnookerBall.Red) {
                        null // Po wbiciu czerwonej, każda kolorowa może zostać wbita
                    } else if (tempRedsRemaining == 0) {
                        when (pottedBall) {
                            SnookerBall.Red -> SnookerBall.Yellow // Nie powinno się zdarzyć w końcówce gry, jeśli sekwencja jest przestrzegana
                            SnookerBall.Yellow -> SnookerBall.Green
                            SnookerBall.Green -> SnookerBall.Brown
                            SnookerBall.Brown -> SnookerBall.Blue
                            SnookerBall.Blue -> SnookerBall.Pink
                            SnookerBall.Pink -> SnookerBall.Black
                            else -> null // Powinno być Black w tym momencie, jeśli wszystkie kolory w sekwencji wbite
                        }
                    } else {
                        tempNextColorBallOn // Zachowaj aktualne nextColorBallOn, jeśli nie jest to czerwona lub nie jest to końcówka gry
                    }

                    // Sprawdź, czy frejm powinien się zakończyć (po wbiciu czarnej w końcówce gry)
                    if (pottedBall is SnookerBall.Black && tempRedsRemaining == 0 && tempNextColorBallOn == SnookerBall.Black) {
                         tempIsFrameOver = true
                    }
                    tempIsFreeBall = false // Wolna bila skonsumowana
                    tempActivePlayerId = currentPlayerTurnId // Gracz kontynuuje turę
                }
                ShotType.FOUL -> {
                    // Punkty za faul dodane przeciwnikowi
                    val opponentId = if (currentPlayerTurnId == player1Id) player2Id else player1Id
                    if (opponentId == player1Id) {
                        tempPlayer1Score += shot.points
                    } else if (opponentId == player2Id) {
                        tempPlayer2Score += shot.points
                    }
                    
                    tempRedsRemaining = (tempRedsRemaining - shot.redsPottedInFoul).coerceAtLeast(0)

                    // Zresetuj brejk
                    tempCurrentBreak = 0
                    tempBreakHistory.clear()
                    tempIsFreeBall = false // Wolna bila mogła zostać ustawiona przez poprzedni faul, ale nie przez ten
                    tempCanPotColor = tempRedsRemaining == 0 // Jeśli brak czerwonych, można wbijać kolorowe
                    // Logika następnego koloru dla fauli może wymagać dopracowania, ale na razie jest prosta.
                    tempNextColorBallOn = if (tempRedsRemaining == 0) SnookerBall.Yellow else null // Po faulu, jeśli brak czerwonych, zacznij od żółtej

                    // Zmiana tury
                    tempActivePlayerId = opponentId
                    currentPlayerTurnId = opponentId // Aktualizuj dla następnego strzału w sekwencji
                }
                ShotType.SAFETY, ShotType.MISS -> {
                    // Zmiana tury, wyniki nie. Brejk się resetuje.
                    val opponentId = if (currentPlayerTurnId == player1Id) player2Id else player1Id
                    tempActivePlayerId = opponentId
                    tempCurrentBreak = 0
                    tempBreakHistory.clear()
                    tempIsFreeBall = false // Status wolnej bili resetuje się przy zmianie tury
                    tempCanPotColor = tempRedsRemaining == 0 // Jeśli brak czerwonych, można wbijać kolorowe
                    tempNextColorBallOn = if (tempRedsRemaining == 0) tempNextColorBallOn ?: SnookerBall.Yellow else null

                    currentPlayerTurnId = opponentId // Aktualizuj dla następnego strzału w sekwencji
                }
            }
        }

        // Oblicz pozostałe punkty na koniec rekonstrukcji
        val finalPointsRemaining = calculatePointsRemaining(tempRedsRemaining, tempNextColorBallOn)

        return ScoringState(
            player1 = uiState.value.player1?.user?.let { user ->
                PlayerState(user = user, score = tempPlayer1Score)
            },
            player2 = uiState.value.player2?.user?.let { user ->
                PlayerState(user = user, score = tempPlayer2Score)
            },
            activePlayerId = tempActivePlayerId,
            currentBreak = tempCurrentBreak,
            redsRemaining = tempRedsRemaining,
            pointsRemaining = finalPointsRemaining,
            breakHistory = tempBreakHistory,
            canPotColor = tempCanPotColor,
            nextColorBallOn = tempNextColorBallOn,
            isFreeBall = tempIsFreeBall,
            isFrameOver = tempIsFrameOver,
            initialReds = initialReds // Zachowaj początkowe czerwone
        )
    }

    fun onUndoClicked() {
        val match = currentMatch ?: return
        val currentFrame = uiState.value.currentFrame ?: return
        
        if (currentFrame.shots.isEmpty()) {
            snackbarManager.showMessage("Brak ruchów do cofnięcia w bieżącym frejmie.")
            return
        }

        val lastShot = currentFrame.shots.last()
        val updatedShots = currentFrame.shots.dropLast(1)

        // Zrekonstruuj stan do przedostatniego strzału
        val reconstructedState = reconstructScoringState(
            shots = updatedShots,
            initialReds = uiState.value.initialReds, // Użyj initialReds z uiState
            player1Id = match.player1Id,
            player2Id = match.player2Id
        )

        // Utwórz zaktualizowany frejm na podstawie zrekonstruowanego stanu
        val updatedFrame = currentFrame.copy(
            player1Points = reconstructedState.player1?.score ?: 0,
            player2Points = reconstructedState.player2?.score ?: 0,
            shots = updatedShots
        )

        updateFrameInMatch(match, updatedFrame)

        // _uiState zostanie automatycznie zaktualizowany poprzez obserwator getMatchStream.
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
    
    fun onNextFrameClicked() {
        val match = currentMatch ?: return
        val state = uiState.value
        
        resetTimer()
        
        val newFrame = Frame(
            matchId = match.id,
            frameNumber = (state.currentFrame?.frameNumber ?: 0) + 1,
            player1Points = 0,
            player2Points = 0,
            shots = emptyList()
        )
        
        val updatedMatch = match.copy(frames = match.frames + newFrame)
        updateMatchInRepository(updatedMatch)

        _uiState.update {
            it.copy(
                redsRemaining = it.initialReds,
                pointsRemaining = calculatePointsRemaining(it.initialReds, null),
                currentBreak = 0,
                breakHistory = emptyList(),
                canPotColor = false,
                nextColorBallOn = null,
                isFreeBall = false,
                isFrameOver = false, 
                showFrameOverDialog = false
            )
        }
    }
    
    fun onEndFrameClicked() {
        val state = uiState.value
        val p1Score = state.player1?.score ?: 0
        val p2Score = state.player2?.score ?: 0

        if (p1Score == p2Score) {
            snackbarManager.showMessage("Nie można zakończyć frejma przy remisie.")
            return
        }

        if (abs(p1Score - p2Score) < state.pointsRemaining) {
            snackbarManager.showMessage("Przewaga jest mniejsza niż liczba punktów na stole.")
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

        _uiState.update {
            it.copy(
                redsRemaining = it.initialReds,
                pointsRemaining = calculatePointsRemaining(it.initialReds, null),
                currentBreak = 0,
                breakHistory = emptyList(),
                canPotColor = false,
                nextColorBallOn = null,
                isFreeBall = false,
                isFrameOver = false,
                showRepeatFrameDialog = false
            )
        }
    }
    
    fun onEndMatchClicked() {
        val state = uiState.value
        val p1Score = state.player1?.score ?: 0
        val p2Score = state.player2?.score ?: 0

        if (abs(p1Score - p2Score) < state.pointsRemaining && state.pointsRemaining > 0) {
            snackbarManager.showMessage("Musisz najpierw zakończyć bieżącego frejma.")
            return
        }
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
package com.example.snookerstats.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.ShotType
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.repository.StatsRepository
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BreakInfo(val value: Int, val date: Long)

data class AllStats(
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val highestBreak: BreakInfo? = null,
    val totalPoints: Int = 0,
    val totalFrames: Int = 0,
    val averageBreak: Int = 0,
    val totalBreaks: Int = 0,
    val successfulSafeties: Int = 0,
    val totalSafetyAttempts: Int = 0,
    val successfulSnookerEscapes: Int = 0,
    val totalSnookerEscapeAttempts: Int = 0,
    val fouls: Int = 0,
    val pointsConcededFromFouls: Int = 0,
    val breaks20plus: Int = 0,
    val breaks50plus: Int = 0,
    val breaks100plus: Int = 0
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _stats = MutableStateFlow<Resource<AllStats>>(Resource.Loading)
    val stats = _stats.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            statsRepository.getAllMatches(userId).collect {
                if (it is Resource.Success) {
                    val matches = it.data
                    if (matches.isNotEmpty()) {
                        val userShots = matches.flatMap { it.frames }.flatMap { it.shots }.filter { shot -> shot.playerId == userId }
                        val totalPoints = userShots.sumOf { it.points }
                        val breaks = mutableListOf<BreakInfo>()
                        var currentBreak = 0
                        var lastShotTimestamp = 0L

                        matches.flatMap { it.frames }.forEach { frame ->
                            frame.shots.forEach { shot ->
                                if (shot.playerId == userId && shot.points > 0) {
                                    currentBreak += shot.points
                                    lastShotTimestamp = shot.timestamp
                                } else {
                                    if (currentBreak > 0) {
                                        breaks.add(BreakInfo(currentBreak, lastShotTimestamp))
                                    }
                                    currentBreak = 0
                                }
                            }
                            if (currentBreak > 0) { // Add break at the end of a frame
                                breaks.add(BreakInfo(currentBreak, lastShotTimestamp))
                                currentBreak = 0 // Reset for next frame
                            }
                        }

                        val matchesWon = matches.count { match ->
                            val userFramesWon = match.frames.count { frame ->
                                val framePlayer1Points = frame.shots.filter { it.playerId == match.player1Id }.sumOf { it.points }
                                val framePlayer2Points = frame.shots.filter { it.playerId == match.player2Id }.sumOf { it.points }
                                (match.player1Id == userId && framePlayer1Points > framePlayer2Points) || (match.player2Id == userId && framePlayer2Points > framePlayer1Points)
                            }
                            val opponentFramesWon = match.frames.size - userFramesWon
                            userFramesWon > opponentFramesWon
                        }

                        val fouls = userShots.count { it.type == ShotType.FOUL }
                        val pointsConcededFromFouls = userShots.filter { it.type == ShotType.FOUL }.sumOf { it.points }

                        val breakValues = breaks.map { it.value }

                        val allStats = AllStats(
                            matchesPlayed = matches.size,
                            matchesWon = matchesWon,
                            highestBreak = breaks.maxByOrNull { it.value },
                            totalPoints = totalPoints,
                            totalFrames = matches.sumOf { it.frames.size },
                            averageBreak = if (breakValues.isNotEmpty()) breakValues.average().toInt() else 0,
                            totalBreaks = breaks.size,
                            successfulSafeties = userShots.count { it.type == ShotType.SAFETY },
                            totalSafetyAttempts = userShots.count { it.type == ShotType.SAFETY || it.type == ShotType.MISS },
                            successfulSnookerEscapes = userShots.count { it.wasSnookered && it.type != ShotType.FOUL },
                            totalSnookerEscapeAttempts = userShots.count { it.wasSnookered },
                            fouls = fouls,
                            pointsConcededFromFouls = pointsConcededFromFouls,
                            breaks20plus = breakValues.count { it >= 20 },
                            breaks50plus = breakValues.count { it >= 50 },
                            breaks100plus = breakValues.count { it >= 100 }
                        )
                        _stats.value = Resource.Success(allStats)
                    } else {
                        _stats.value = Resource.Success(AllStats())
                    }
                } else if (it is Resource.Error) {
                    _stats.value = Resource.Error(it.message)
                }
            }
        }
    }
}

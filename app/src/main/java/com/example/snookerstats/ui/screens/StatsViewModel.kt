package com.example.snookerstats.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.MatchType
import com.example.snookerstats.domain.model.ShotType
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.repository.StatsRepository
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BreakInfo(val value: Int, val date: Long)

data class StatsFilters(
    val matchType: MatchType? = null,
    val numberOfReds: Int? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)

data class AllStats(
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val matchesLost: Int = 0,
    val matchesDrawn: Int = 0,
    val last5Matches: List<String> = emptyList(),
    val highestBreak: BreakInfo? = null,
    val totalPoints: Int = 0,
    val opponentTotalPoints: Int = 0, // Added opponent's points
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

    private val _isFilterSheetVisible = MutableStateFlow(false)
    val isFilterSheetVisible = _isFilterSheetVisible.asStateFlow()

    private val _filters = MutableStateFlow(StatsFilters())
    val filters = _filters.asStateFlow()

    init {
        loadStats()
    }

    fun onFilterClick() {
        _isFilterSheetVisible.value = true
    }

    fun onFilterSheetDismiss() {
        _isFilterSheetVisible.value = false
    }

    fun applyFilters(newFilters: StatsFilters) {
        _filters.value = newFilters
        loadStats() // Reload stats with new filters
        onFilterSheetDismiss()
    }

    fun onFilterChipClosed(filterType: StatsFilterType) {
        val currentFilters = _filters.value
        _filters.value = when (filterType) {
            StatsFilterType.MATCH_TYPE -> currentFilters.copy(matchType = null)
            StatsFilterType.NUMBER_OF_REDS -> currentFilters.copy(numberOfReds = null)
            StatsFilterType.START_DATE -> currentFilters.copy(startDate = null)
            StatsFilterType.END_DATE -> currentFilters.copy(endDate = null)
        }
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            statsRepository.getAllMatches(userId).collect {
                if (it is Resource.Success) {
                    val filteredMatches = it.data.filter { match ->
                        (_filters.value.matchType == null || match.matchType == _filters.value.matchType) &&
                        (_filters.value.numberOfReds == null || match.numberOfReds == _filters.value.numberOfReds) &&
                        (_filters.value.startDate == null || match.date >= _filters.value.startDate!!) &&
                        (_filters.value.endDate == null || match.date <= _filters.value.endDate!!)
                    }
                    val matches = filteredMatches.sortedByDescending { it.date } // Sort matches by date
                    if (matches.isNotEmpty()) {
                        val allShots = matches.flatMap { it.frames }.flatMap { it.shots }
                        val userShots = allShots.filter { shot -> shot.playerId == userId }
                        val opponentShots = allShots.filter { shot -> shot.playerId != userId }

                        val totalPoints = userShots.sumOf { it.points }
                        val opponentTotalPoints = opponentShots.sumOf { it.points }

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

                        var matchesWon = 0
                        var matchesLost = 0
                        var matchesDrawn = 0
                        val last5Matches = mutableListOf<String>()

                        matches.forEach { match ->
                            val p1FramesWon = match.frames.count { it.player1Points > it.player2Points }
                            val p2FramesWon = match.frames.count { it.player2Points > it.player1Points }

                            val userFramesWon: Int
                            val opponentFramesWon: Int

                            if (match.player1Id == userId) {
                                userFramesWon = p1FramesWon
                                opponentFramesWon = p2FramesWon
                            } else {
                                userFramesWon = p2FramesWon
                                opponentFramesWon = p1FramesWon
                            }

                            val result = when {
                                userFramesWon > opponentFramesWon -> {
                                    matchesWon++
                                    "W"
                                }
                                userFramesWon < opponentFramesWon -> {
                                    matchesLost++
                                    "P"
                                }
                                else -> {
                                    matchesDrawn++
                                    "R"
                                }
                            }
                            if (last5Matches.size < 5) {
                                last5Matches.add(result)
                            }
                        }

                        val fouls = userShots.count { it.type == ShotType.FOUL }
                        val pointsConcededFromFouls = userShots.filter { it.type == ShotType.FOUL }.sumOf { it.points }

                        val breakValues = breaks.map { it.value }

                        val allStats = AllStats(
                            matchesPlayed = matches.size,
                            matchesWon = matchesWon,
                            matchesLost = matchesLost,
                            matchesDrawn = matchesDrawn,
                            last5Matches = last5Matches,
                            highestBreak = breaks.maxByOrNull { it.value },
                            totalPoints = totalPoints,
                            opponentTotalPoints = opponentTotalPoints, // Pass opponent's points
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
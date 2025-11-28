package com.example.snookerstats.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.AuthRepository
import com.example.snookerstats.domain.repository.MatchRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.ui.screens.MatchHistoryDisplayItem
import com.example.snookerstats.ui.screens.MatchResult
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TimeFilter { LAST_7_DAYS, LAST_30_DAYS, ALL_TIME }

data class HistoryFilters(
    val timeFilter: TimeFilter = TimeFilter.ALL_TIME,
    val opponent: User? = null,
    val matchType: com.example.snookerstats.domain.model.MatchType? = null
)

@HiltViewModel
class MatchHistoryViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _matches = MutableStateFlow<List<MatchHistoryDisplayItem>>(emptyList())
    val matches = _matches.asStateFlow()

    private val _filters = MutableStateFlow(HistoryFilters())
    val filters = _filters.asStateFlow()

    private val _isFilterSheetVisible = MutableStateFlow(false)
    val isFilterSheetVisible = _isFilterSheetVisible.asStateFlow()

    private val currentUserId: String? = authRepository.currentUser?.uid

    init {
        loadMatchHistory()
    }

    fun onFilterClick() {
        _isFilterSheetVisible.value = true
    }

    fun onFilterSheetDismiss() {
        _isFilterSheetVisible.value = false
    }

    fun applyFilters(newFilters: HistoryFilters) {
        _filters.value = newFilters
        // The collector will automatically re-trigger
        onFilterSheetDismiss()
    }

    private fun loadMatchHistory() {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                matchRepository.getMatchesForUserStream(userId).collectLatest { allMatches ->
                    val filteredMatches = allMatches.filter { match ->
                        val timeFilterApplies = when (filters.value.timeFilter) {
                            TimeFilter.LAST_7_DAYS -> match.date >= System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
                            TimeFilter.LAST_30_DAYS -> match.date >= System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
                            TimeFilter.ALL_TIME -> true
                        }
                        val opponentFilterApplies = filters.value.opponent?.let {
                            match.participants.contains(it.uid)
                        } ?: true

                        val matchTypeFilterApplies = filters.value.matchType?.let {
                            match.matchType == it
                        } ?: true

                        timeFilterApplies && opponentFilterApplies && matchTypeFilterApplies && !match.hiddenFor.contains(currentUserId)
                    }

                    val displayItems = coroutineScope {
                        filteredMatches.map { match ->
                            async {
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

                                    val result = when {
                                        (match.player1Id == userId && p1FramesWon > p2FramesWon) || (match.player2Id == userId && p2FramesWon > p1FramesWon) -> MatchResult.WIN
                                        p1FramesWon == p2FramesWon -> MatchResult.DRAW
                                        else -> MatchResult.LOSS
                                    }

                                    MatchHistoryDisplayItem(
                                        match = match,
                                        player1 = player1,
                                        player2 = player2,
                                        p1FramesWon = p1FramesWon,
                                        p2FramesWon = p2FramesWon,
                                        result = result
                                    )
                                } else {
                                    null
                                }
                            }
                        }.awaitAll().filterNotNull()
                    }
                    _matches.value = displayItems.sortedByDescending { it.match.date }
                }
            }
        }
    }

    fun hideMatch(matchId: String) {
        viewModelScope.launch {
            matchRepository.hideMatchForUser(matchId, currentUserId ?: "")
        }
    }
}

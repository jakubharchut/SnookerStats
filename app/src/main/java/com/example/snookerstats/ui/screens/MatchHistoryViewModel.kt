package com.example.snookerstats.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.repository.MatchRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchHistoryViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _matches = MutableStateFlow<List<MatchHistoryDisplayItem>>(emptyList())
    val matches = _matches.asStateFlow()

    init {
        loadMatchHistory()
    }

    private fun loadMatchHistory() {
        viewModelScope.launch {
            matchRepository.getAllMatchesStream().collect { matches ->
                val displayItems = matches.mapNotNull { match ->
                    val player1Res = userRepository.getUser(match.player1Id)
                    val player2Res = match.player2Id?.let { userRepository.getUser(it) }

                    val player1 = (player1Res as? Resource.Success)?.data
                    val player2 = (player2Res as? Resource.Success)?.data

                    // Calculate frame wins
                    val p1FramesWon = match.frames.count { it.player1Points > it.player2Points }
                    val p2FramesWon = match.frames.count { it.player2Points > it.player1Points }

                    MatchHistoryDisplayItem(
                        match = match,
                        player1 = player1,
                        player2 = player2,
                        p1FramesWon = p1FramesWon,
                        p2FramesWon = p2FramesWon
                    )
                }
                _matches.value = displayItems.sortedByDescending { it.match.date }
            }
        }
    }
}

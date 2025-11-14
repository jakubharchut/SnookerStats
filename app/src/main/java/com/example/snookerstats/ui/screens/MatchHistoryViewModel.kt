package com.example.snookerstats.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.IAuthRepository
import com.example.snookerstats.domain.repository.MatchRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchHistoryViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _matches = MutableStateFlow<List<MatchHistoryDisplayItem>>(emptyList())
    val matches = _matches.asStateFlow()

    private val currentUserId: String? = authRepository.currentUser?.uid

    init {
        loadMatchHistory()
    }

    private fun loadMatchHistory() {
        viewModelScope.launch {
            matchRepository.getAllMatchesStream().collect { matches ->
                val visibleMatches = matches.filter { !it.hiddenFor.contains(currentUserId) }

                val displayItems = coroutineScope {
                    visibleMatches.map { match ->
                        async {
                            // Correctly call the suspend function to get the Resource for Player 1
                            val p1Resource = userRepository.getUser(match.player1Id)
                            val player1 = if (p1Resource is Resource.Success) p1Resource.data else null

                            // Handle guest or fetch player 2
                            val player2: User? = if (match.player2Id?.startsWith("guest_") == true) {
                                val guestName = match.player2Id.removePrefix("guest_")
                                User(uid = match.player2Id, username = guestName, firstName = guestName, lastName = "")
                            } else {
                                match.player2Id?.let {
                                    // Correctly call the suspend function for Player 2
                                    val p2Resource = userRepository.getUser(it)
                                    if (p2Resource is Resource.Success) p2Resource.data else null
                                }
                            }

                            // Create the display item if player1 was found
                            if (player1 != null) {
                                val p1FramesWon = match.frames.count { it.player1Points > it.player2Points }
                                val p2FramesWon = match.frames.count { it.player2Points > it.player1Points }
                                MatchHistoryDisplayItem(
                                    match = match,
                                    player1 = player1,
                                    player2 = player2,
                                    p1FramesWon = p1FramesWon,
                                    p2FramesWon = p2FramesWon
                                )
                            } else {
                                null // This item will be filtered out later
                            }
                        }
                    }.awaitAll().filterNotNull() // Await all async calls and remove any nulls
                }
                _matches.value = displayItems.sortedByDescending { it.match.date }
            }
        }
    }

    fun hideMatch(matchId: String) {
        viewModelScope.launch {
            matchRepository.hideMatchForUser(matchId, currentUserId ?: "")
        }
    }
}

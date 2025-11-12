package com.example.snookerstats.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.domain.repository.IAuthRepository
import com.example.snookerstats.domain.repository.UserRepository
import com.example.snookerstats.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerLists(
    val favorites: List<User> = emptyList(),
    val clubMembers: List<User> = emptyList(),
    val otherFriends: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val allFriends: List<User> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val currentUser: User? = null // Store current user for re-grouping
)

@HiltViewModel
class PlayScreenViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _playerLists = MutableStateFlow(PlayerLists())
    val playerLists = _playerLists.asStateFlow()

    init {
        loadFriends()
    }

    fun onToggleFavorite(favoriteUserId: String) {
        // 1. Optimistic UI Update
        val currentState = _playerLists.value
        val newFavoriteIds = if (favoriteUserId in currentState.favoriteIds) {
            currentState.favoriteIds - favoriteUserId
        } else {
            currentState.favoriteIds + favoriteUserId
        }

        // Re-partition immediately using stored data
        currentState.currentUser?.let { user ->
            val (favorites, nonFavorites) = currentState.allFriends.partition { it.uid in newFavoriteIds }
            val clubMembers = nonFavorites.filter { it.club == user.club && it.club?.isNotBlank() == true }
            val otherFriends = nonFavorites.filter { it !in clubMembers }

            _playerLists.value = currentState.copy(
                favorites = favorites,
                clubMembers = clubMembers,
                otherFriends = otherFriends,
                favoriteIds = newFavoriteIds
            )
        }

        // 2. Fire-and-forget the database call
        viewModelScope.launch {
            val currentUserId = authRepository.currentUser?.uid ?: return@launch
            userRepository.toggleFavorite(currentUserId, favoriteUserId)
        }
    }

    private fun loadFriends() {
        viewModelScope.launch {
            _playerLists.value = _playerLists.value.copy(isLoading = true)
            val currentUserId = authRepository.currentUser?.uid ?: return@launch

            when (val currentUserRes = userRepository.getUser(currentUserId)) {
                is Resource.Success -> {
                    val currentUser = currentUserRes.data
                    if (currentUser == null) {
                        _playerLists.value = PlayerLists(isLoading = false)
                        return@launch
                    }

                    val friendUsers = mutableListOf<User>()
                    for (friendId in currentUser.friends) {
                        if (friendId == currentUserId) continue
                        when (val friendRes = userRepository.getUser(friendId)) {
                            is Resource.Success -> friendRes.data?.let { friendUsers.add(it) }
                            else -> {}
                        }
                    }

                    val favoriteIds = currentUser.favorites.toSet()
                    val (favorites, nonFavorites) = friendUsers.partition { it.uid in favoriteIds }
                    val clubMembers = nonFavorites.filter { it.club == currentUser.club && it.club?.isNotBlank() == true }
                    val otherFriends = nonFavorites.filter { it !in clubMembers }

                    _playerLists.value = PlayerLists(
                        favorites = favorites,
                        clubMembers = clubMembers,
                        otherFriends = otherFriends,
                        isLoading = false,
                        allFriends = friendUsers,
                        favoriteIds = favoriteIds,
                        currentUser = currentUser
                    )
                }
                else -> {
                    _playerLists.value = PlayerLists(isLoading = false)
                }
            }
        }
    }
}

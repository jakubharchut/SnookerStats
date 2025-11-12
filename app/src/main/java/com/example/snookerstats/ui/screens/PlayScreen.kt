package com.example.snookerstats.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.ui.common.UserAvatar
import com.example.snookerstats.ui.community.CommunityViewModel
import com.example.snookerstats.util.Resource

@Composable
fun PlayScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Gracze", "Gość", "Trening", "Turniej")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> PlayerContent(navController)
            1 -> GuestContent(navController)
            2 -> TrainingContent(navController)
            3 -> TournamentContent()
        }
    }
}

@Composable
private fun PlayerContent(navController: NavController) {
    OpponentSelectionList(navController = navController)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OpponentSelectionList(navController: NavController, viewModel: CommunityViewModel = hiltViewModel()) {
    val friendsState by viewModel.friends.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var expandedHeaders by remember { mutableStateOf(setOf("Ulubieni", "Klubowicze", "Pozostali znajomi")) }

    LaunchedEffect(Unit) {
        viewModel.fetchFriends()
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        when (val state = friendsState) {
            is Resource.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is Resource.Success -> {
                val friends = state.data
                
                val groupedOpponents = remember(friends, currentUser) {
                    val favIds = currentUser?.favorites ?: emptyList() // POPRAWIONE
                    val currentUserClub = currentUser?.club?.takeIf { it.isNotBlank() }

                    val (favorites, nonFavorites) = friends.partition { it.uid in favIds }
                    val (clubFriends, otherFriends) = nonFavorites.partition { it.club != null && it.club == currentUserClub }

                    linkedMapOf(
                        "Ulubieni" to favorites,
                        "Klubowicze" to clubFriends,
                        "Pozostali znajomi" to otherFriends
                    )
                }

                if (friends.isEmpty()) {
                     Text("Nie masz jeszcze żadnych znajomych. Dodaj ich w zakładce 'Ludzie' lub wyszukaj poniżej.", textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                }
                
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    groupedOpponents.forEach { (header, opponents) ->
                        stickyHeader {
                            val isExpanded = header in expandedHeaders
                            Surface(
                                modifier = Modifier.fillParentMaxWidth().clickable {
                                    expandedHeaders = if (isExpanded) expandedHeaders - header else expandedHeaders + header
                                },
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = header, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isExpanded) "Zwiń" else "Rozwiń"
                                    )
                                }
                            }
                        }

                        if (header in expandedHeaders) {
                            if (opponents.isNotEmpty()) {
                                items(opponents) { opponent ->
                                    val isFavorite = currentUser?.favorites?.contains(opponent.uid) == true // POPRAWIONE
                                    ListItem(
                                        headlineContent = { Text(opponent.username, fontWeight = FontWeight.Bold) },
                                        supportingContent = { Text("${opponent.firstName} ${opponent.lastName}") },
                                        leadingContent = { UserAvatar(user = opponent, modifier = Modifier.size(40.dp)) },
                                        trailingContent = {
                                            IconButton(onClick = {
                                                if (isFavorite) viewModel.removeFromFavorites(opponent.uid)
                                                else viewModel.addToFavorites(opponent.uid)
                                            }) {
                                                Icon(
                                                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                                    contentDescription = if (isFavorite) "Usuń z ulubionych" else "Dodaj do ulubionych",
                                                    tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray
                                                )
                                            }
                                        },
                                        modifier = Modifier.clickable { navController.navigate("match_setup/${opponent.uid}") }
                                    )
                                }
                            } else {
                                item {
                                    val emptyText = when(header) {
                                        "Klubowicze" -> if (currentUser?.club.isNullOrBlank()) "Nie należysz do żadnego klubu." else "Brak innych graczy w Twoim klubie."
                                        "Ulubieni" -> "Brak ulubionych graczy."
                                        else -> "Brak graczy w tej kategorii."
                                    }
                                    Text(
                                        text = emptyText,
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center
                                    )
                                 }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { navController.navigate("community?initialTabIndex=0") },
                    modifier = Modifier.fillMaxWidth(0.8f).padding(bottom = 16.dp)
                ) {
                    Text("Szukaj gracza")
                }
            }
            is Resource.Error -> Text("Błąd: ${state.message}", color = MaterialTheme.colorScheme.error)
        }
    }
}


@Composable
private fun GuestContent(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight().padding(16.dp)
    ) {
        Text("Zapisz wynik meczu z osobą, która nie posiada konta w aplikacji.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("match_setup/guest") },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Zagraj z Gościem")
        }
    }
}

@Composable
private fun TrainingContent(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight().padding(16.dp)
    ) {
        Text("Rozpocznij sesję treningową w pojedynkę.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("match_setup/solo") },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Trening Solo")
        }
    }
}

@Composable
private fun TournamentContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight().padding(16.dp)
    ) {
        Text("Funkcjonalność w budowie...\nStay tuned! ;)", textAlign = TextAlign.Center)
    }
}

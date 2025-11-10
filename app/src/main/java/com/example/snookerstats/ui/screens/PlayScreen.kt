package com.example.snookerstats.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var selectedTabIndex by remember { mutableIntStateOf(0) }
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
    // To jest teraz główna zawartość zakładki "Gracze"
    OpponentSelectionList(navController = navController)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OpponentSelectionList(navController: NavController, viewModel: CommunityViewModel = hiltViewModel()) {
    val friendsState by viewModel.friends.collectAsState()
    val currentUserState by viewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchFriends()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        when (val state = friendsState) {
            is Resource.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is Resource.Success -> {
                val friends = state.data
                val currentUser = currentUserState

                val clubMembersOnly = emptyList<User>()

                val groupedOpponents = remember(friends, currentUser) {
                    val currentUserClub = currentUser?.club?.takeIf { it.isNotBlank() }
                    val (clubFriends, otherFriends) = friends.partition {
                        it.club != null && it.club == currentUserClub
                    }
                    
                    linkedMapOf(
                        "Klubowicze" to clubFriends,
                        "Pozostali klubowicze" to clubMembersOnly,
                        "Pozostali znajomi" to otherFriends
                    )
                }

                if (friends.isEmpty() && clubMembersOnly.isEmpty()) {
                     Text("Nie masz jeszcze żadnych znajomych. Dodaj ich w zakładce 'Ludzie' lub wyszukaj poniżej.", textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                }
                
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    groupedOpponents.forEach { (header, opponents) ->
                        if (opponents.isNotEmpty() || header.contains("Klubowicze")) {
                            stickyHeader {
                                Surface(modifier = Modifier.fillParentMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text(
                                        text = header,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        if (opponents.isNotEmpty()) {
                            items(opponents) { opponent ->
                                ListItem(
                                    headlineContent = { Text(opponent.username, fontWeight = FontWeight.Bold) },
                                    supportingContent = { Text("${opponent.firstName} ${opponent.lastName}") },
                                    leadingContent = { UserAvatar(user = opponent, modifier = Modifier.size(40.dp)) },
                                    modifier = Modifier.clickable { navController.navigate("match_setup/${opponent.uid}") }
                                )
                            }
                        } else if (header.contains("Klubowicze")) {
                            item {
                                Text(
                                    text = if(header == "Klubowicze") "Brak znajomych w Twoim klubie." else "Funkcjonalność wkrótce dostępna.",
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { navController.navigate("community?initialTabIndex=0") },
                    modifier = Modifier.fillMaxWidth(0.8f)
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

package com.example.snookerstats.ui.screens

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
    val tabs = listOf("Gracz", "Gość", "Trening", "Turniej")

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
    var showOpponentList by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showOpponentList) {
            OpponentSelectionList(navController = navController)
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text("Rozpocznij mecz z innym zarejestrowanym graczem.", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showOpponentList = true },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Wybierz Gracza")
                }
            }
        }
    }
}

@Composable
private fun OpponentSelectionList(navController: NavController, viewModel: CommunityViewModel = hiltViewModel()) {
    val friendsState by viewModel.friends.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchFriends()
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        when (val state = friendsState) {
            is Resource.Loading -> CircularProgressIndicator()
            is Resource.Success -> {
                if (state.data.isEmpty()) {
                    Text("Nie masz jeszcze żadnych znajomych. Dodaj ich w zakładce 'Ludzie'.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.data) { friend ->
                            ListItem(
                                headlineContent = { Text(friend.username, fontWeight = FontWeight.Bold) },
                                supportingContent = { Text("${friend.firstName} ${friend.lastName}") },
                                leadingContent = { UserAvatar(user = friend, modifier = Modifier.size(40.dp)) },
                                modifier = Modifier.clickable {
                                    navController.navigate("match_setup/${friend.uid}")
                                }
                            )
                        }
                    }
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
        modifier = Modifier.fillMaxHeight()
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
        modifier = Modifier.fillMaxHeight()
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
        modifier = Modifier.fillMaxHeight()
    ) {
        Text("Funkcjonalność w budowie...\nStay tuned! ;)", textAlign = TextAlign.Center)
    }
}

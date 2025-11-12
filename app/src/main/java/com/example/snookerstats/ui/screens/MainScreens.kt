package com.example.snookerstats.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.MatchStatus
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.ui.common.UserAvatar
import com.example.snookerstats.ui.main.MainViewModel
import com.example.snookerstats.ui.navigation.BottomNavItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Ekran Główny")
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { mainViewModel.runDatabaseTest() }) {
                Text("Uruchom Test Bazy Danych")
            }
        }
    }
}

@Composable
fun PlayScreen(
    navController: NavController,
    ongoingMatch: Match?
) {
    LaunchedEffect(ongoingMatch) {
        if (ongoingMatch != null) {
            navController.navigate("scoring/${ongoingMatch.id}/${ongoingMatch.numberOfReds}") {
                navController.graph.findStartDestination().route?.let { route ->
                    popUpTo(route)
                }
            }
        }
    }

    if (ongoingMatch == null) {
        var selectedTabIndex by remember { mutableStateOf(0) }
        val tabs = listOf("Gracze", "Gość", "Trening", "Turniej")

        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> PlayerTabContent(navController = navController)
                1 -> GuestTabContent(navController = navController)
                2 -> TrainingTabContent(navController = navController)
                3 -> TournamentTabContent()
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
            Text("Wznawianie meczu...", modifier = Modifier.padding(top = 64.dp))
        }
    }
}

@Composable
fun PlayerTabContent(
    navController: NavController,
    viewModel: PlayScreenViewModel = hiltViewModel()
) {
    val playerLists by viewModel.playerLists.collectAsState()

    if (playerLists.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                PlayerSection(
                    title = "Ulubieni",
                    players = playerLists.favorites,
                    favoriteIds = playerLists.favoriteIds,
                    onPlayerClick = { user -> navController.navigate("match_setup/${user.uid}") },
                    onToggleFavorite = viewModel::onToggleFavorite
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                PlayerSection(
                    title = "Klubowicze",
                    players = playerLists.clubMembers,
                    favoriteIds = playerLists.favoriteIds,
                    onPlayerClick = { user -> navController.navigate("match_setup/${user.uid}") },
                    onToggleFavorite = viewModel::onToggleFavorite
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                PlayerSection(
                    title = "Pozostali znajomi",
                    players = playerLists.otherFriends,
                    favoriteIds = playerLists.favoriteIds,
                    onPlayerClick = { user -> navController.navigate("match_setup/${user.uid}") },
                    onToggleFavorite = viewModel::onToggleFavorite
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            item {
                OutlinedButton(
                    onClick = {
                        navController.navigate(BottomNavItem.Community.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Szukaj gracza")
                }
            }
        }
    }
}

@Composable
fun PlayerSection(
    title: String,
    players: List<User>,
    favoriteIds: Set<String>,
    onPlayerClick: (User) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                if (players.isEmpty()) {
                    Text(
                        text = "Brak graczy w tej kategorii.",
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    players.forEach { user ->
                        PlayerListItem(
                            user = user,
                            isFavorite = user.uid in favoriteIds,
                            onClick = { onPlayerClick(user) },
                            onToggleFavorite = { onToggleFavorite(user.uid) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerListItem(
    user: User,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text("${user.firstName} ${user.lastName}") },
        supportingContent = { Text("@${user.username}") },
        leadingContent = { UserAvatar(user = user, modifier = Modifier.size(40.dp)) },
        trailingContent = {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Dodaj do ulubionych",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    )
}


@Composable
fun GuestTabContent(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { navController.navigate("match_setup/guest") }) {
            Text("Graj z gościem")
        }
    }
}

@Composable
fun TrainingTabContent(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { navController.navigate("match_setup/solo") }) {
            Text("Graj solo (trening)")
        }
    }
}

@Composable
fun TournamentTabContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Funkcjonalność turniejów wkrótce!")
    }
}

@Composable
fun MatchHistoryScreen(
    viewModel: MatchHistoryViewModel = hiltViewModel()
) {
    val matches by viewModel.matches.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Historia Meczów",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        if (matches.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Brak rozegranych meczów.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(matches) { match ->
                    MatchHistoryItem(match = match)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun MatchHistoryItem(match: Match) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "ID Meczu: ${match.id}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Data: ${formatTimestamp(match.date)}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Gracz 1: ${match.player1Id}", style = MaterialTheme.typography.bodyMedium)
            match.player2Id?.let {
                Text(text = "Gracz 2: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "Typ Meczu: ${match.matchType}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${match.status}", style = MaterialTheme.typography.bodyMedium)

            if (match.status == MatchStatus.COMPLETED) {
                val lastFrame = match.frames.lastOrNull()
                if (lastFrame != null) {
                    val p1Score = lastFrame.player1Points
                    val p2Score = lastFrame.player2Points
                    val winner = if (p1Score > p2Score) match.player1Id else match.player2Id
                    Text(text = "Wynik: $p1Score - $p2Score", style = MaterialTheme.typography.bodyLarge)
                    winner?.let { Text(text = "Zwycięzca: $it", style = MaterialTheme.typography.bodyLarge) }
                }
            }
        }
    }
}

@Composable
fun StatsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Stats Screen")
    }
}

@Composable
fun TournamentsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Tournaments Screen")
    }
}

@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Profile Screen")
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

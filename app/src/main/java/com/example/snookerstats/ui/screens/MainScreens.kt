package com.example.snookerstats.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.ui.common.UserAvatar
import com.example.snookerstats.ui.navigation.BottomNavItem
import com.example.snookerstats.util.Resource
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Ekran Główny")
    }
}

@Composable
fun PlayScreen(
    navController: NavController,
    ongoingMatch: Match?,
    selectedOpponentId: String? = null
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current

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
                0 -> PlayerTabContent(navController = navController, preselectedOpponentId = selectedOpponentId)
                1 -> GuestTabContent(navController = navController)
                2 -> TrainingScreen(navController = navController)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerTabContent(
    navController: NavController,
    viewModel: MatchSetupViewModel = hiltViewModel(),
    preselectedOpponentId: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(preselectedOpponentId) {
        viewModel.loadOpponentDetails(preselectedOpponentId)
    }

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.navigationEvent.collectLatest { route ->
                navController.navigate(route)
            }
        }
    }

    when (val opponent = uiState.opponentType) {
        is OpponentType.PLAYER -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Mecz z ${opponent.user.username}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 32.dp, bottom = 24.dp))
                    UserAvatar(user = opponent.user, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    // Match Type Section
                    Text("Rodzaj Meczu", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            onClick = { viewModel.onMatchTypeChange(MatchType.SPARRING) },
                            selected = uiState.matchType == MatchType.SPARRING
                        ) { Text("Sparingowy") }
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            onClick = { viewModel.onMatchTypeChange(MatchType.RANKING) },
                            selected = uiState.matchType == MatchType.RANKING
                        ) { Text("Rankingowy") }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Match Format Section
                    Text("Format Meczu (liczba czerwonych)", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        MatchFormat.values().forEachIndexed { index, format ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = MatchFormat.values().size),
                                onClick = { viewModel.onMatchFormatChange(format) },
                                selected = uiState.matchFormat == format
                            ) { Text(format.reds.toString()) }
                        }
                    }
                }

                Button(
                    onClick = viewModel::onStartMatchClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Rozpocznij Mecz", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        else -> {
            val playerViewModel: PlayScreenViewModel = hiltViewModel()
            val playerLists by playerViewModel.playerLists.collectAsState()

            if (playerLists.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(bottom = 72.dp)
                    ) {
                        item {
                            PlayerSection(
                                title = "Ulubieni",
                                players = playerLists.favorites,
                                favoriteIds = playerLists.favoriteIds,
                                onPlayerClick = { user -> navController.navigate("play?opponentId=${user.uid}") },
                                onToggleFavorite = playerViewModel::onToggleFavorite
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        item {
                            PlayerSection(
                                title = "Klubowicze",
                                players = playerLists.clubMembers,
                                favoriteIds = playerLists.favoriteIds,
                                onPlayerClick = { user -> navController.navigate("play?opponentId=${user.uid}") },
                                onToggleFavorite = playerViewModel::onToggleFavorite
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        item {
                            PlayerSection(
                                title = "Pozostali znajomi",
                                players = playerLists.otherFriends,
                                favoriteIds = playerLists.favoriteIds,
                                onPlayerClick = { user -> navController.navigate("play?opponentId=${user.uid}") },
                                onToggleFavorite = playerViewModel::onToggleFavorite
                            )
                        }
                    }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        Text("Szukaj gracza")
                    }
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
                    tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestTabContent(navController: NavController, viewModel: MatchSetupViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.loadOpponentDetails("guest")
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.navigationEvent.collectLatest { route ->
                navController.navigate(route)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Mecz z Gościem", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 32.dp, bottom = 24.dp))
            OutlinedTextField(
                value = uiState.guestName,
                onValueChange = viewModel::onGuestNameChange,
                label = { Text("Imię gościa") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Match Type Section
            Text("Rodzaj Meczu", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    onClick = { viewModel.onMatchTypeChange(MatchType.SPARRING) },
                    selected = uiState.matchType == MatchType.SPARRING
                ) { Text("Sparingowy") }
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    onClick = { viewModel.onMatchTypeChange(MatchType.RANKING) },
                    selected = uiState.matchType == MatchType.RANKING
                ) { Text("Rankingowy") }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Match Format Section
            Text("Format Meczu (liczba czerwonych)", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                MatchFormat.values().forEachIndexed { index, format ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = MatchFormat.values().size),
                        onClick = { viewModel.onMatchFormatChange(format) },
                        selected = uiState.matchFormat == format
                    ) { Text(format.reds.toString()) }
                }
            }
        }

        Button(
            onClick = viewModel::onStartMatchClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !(uiState.opponentType is OpponentType.GUEST && uiState.guestName.isBlank())
        ) {
            Text("Rozpocznij Mecz", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun TournamentTabContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Funkcjonalność turniejów wkrótce!")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchHistoryScreen(
    navController: NavController,
    viewModel: MatchHistoryViewModel = hiltViewModel()
) {
    val matches by viewModel.matches.collectAsState()
    val isFilterSheetVisible by viewModel.isFilterSheetVisible.collectAsState()
    val filters by viewModel.filters.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedMatchId by remember { mutableStateOf<String?>(null) }

    if (showDialog && selectedMatchId != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Potwierdź usunięcie") },
            text = { Text("Czy na pewno chcesz usunąć ten mecz ze swojej historii? Ta operacja jest nieodwracalna.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.hideMatch(selectedMatchId!!)
                    showDialog = false
                }) { Text("Usuń") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Anuluj") }
            }
        )
    }

    if (isFilterSheetVisible) {
        MatchHistoryFilterSheet(
            onDismiss = viewModel::onFilterSheetDismiss,
            onApplyFilters = viewModel::applyFilters,
            initialFilters = filters
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historia Meczów") },
                actions = {
                    IconButton(onClick = viewModel::onFilterClick) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtruj")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (matches.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Brak rozegranych meczów.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(matches) { item ->
                        MatchHistoryItem(
                            item = item,
                            onClick = { navController.navigate("match_details/${item.match.id}") },
                            onDeleteClick = {
                                selectedMatchId = item.match.id
                                showDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MatchHistoryItem(item: MatchHistoryDisplayItem, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(item.match.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Usuń mecz", modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Player 1
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    UserAvatar(user = item.player1, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.player1?.username ?: "Gracz 1",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Score
                Text(
                    text = "${item.p1FramesWon} - ${item.p2FramesWon}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Player 2
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    UserAvatar(user = item.player2, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.player2?.username ?: "Gracz 2",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.match.matchType.name,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Mecze", "Trening")

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
            0 -> MatchStatsContent(viewModel)
            1 -> TrainingStatsContent()
        }
    }
}

@Composable
fun MatchStatsContent(viewModel: StatsViewModel) {
    val statsResource by viewModel.stats.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (val resource = statsResource) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val stats = resource.data
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StatItem(icon = Icons.Default.EmojiEvents, label = "Rozegrane mecze", value = stats.matchesPlayed.toString())
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        StatItem(icon = Icons.Default.TrendingUp, label = "Punkty łącznie", value = stats.totalPoints.toString())
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        StatItem(icon = Icons.Default.Star, label = "Najwyższy break", value = stats.highestBreak.toString())
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        StatItem(icon = Icons.Default.Functions, label = "Średni break", value = stats.averageBreak.toString())
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        StatItem(icon = Icons.Default.BarChart, label = "Brejki 25+", value = stats.breaks25plus.toString())
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        StatItem(icon = Icons.Default.MilitaryTech, label = "Breaki 50+", value = stats.breaks50plus.toString())
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        StatItem(icon = Icons.Default.WorkspacePremium, label = "Breaki 100+", value = stats.breaks100plus.toString())
                    }
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Błąd ładowania statystyk: ${resource.message}")
                }
            }
        }
    }
}

@Composable
fun TrainingStatsContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Statystyki treningów będą dostępne wkrótce!")
    }
}

@Composable
fun StatItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
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

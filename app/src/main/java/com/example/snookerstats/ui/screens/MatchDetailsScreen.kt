package com.example.snookerstats.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.snookerstats.domain.model.Frame
import com.example.snookerstats.domain.model.ShotType
import com.example.snookerstats.domain.model.SnookerBall
import com.example.snookerstats.domain.model.User
import com.example.snookerstats.ui.common.UserAvatar
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    navController: NavController,
    matchId: String?
) {
    val viewModel: MatchDetailsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(matchId) {
        if (matchId != null) {
            viewModel.loadMatchDetails(matchId)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { navController.popBackStack() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Cofnij", style = MaterialTheme.typography.bodyLarge)
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.matchItem != null -> {
                val matchItem = uiState.matchItem!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MatchHeader(item = matchItem)
                    Spacer(modifier = Modifier.height(16.dp))

                    SegmentedButtonRow(
                        selectedTabIndex = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedTab == 0) {
                        DetailsTab(
                            matchStats = uiState.matchStats,
                            frameDetails = uiState.frameDetails,
                            frames = matchItem.match.frames,
                            player1 = uiState.matchItem?.player1,
                            player2 = uiState.matchItem?.player2
                        )
                    } else {
                        HistoryTab(
                            matchItem = matchItem,
                            frameHistories = uiState.frameHistories
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchHeader(item: MatchHistoryDisplayItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            UserAvatar(user = item.player1, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${item.player1?.firstName ?: ""} ${item.player1?.lastName ?: ""}".trim(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "@${item.player1?.username ?: "Gracz 1"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "${item.p1FramesWon} - ${item.p2FramesWon}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            UserAvatar(user = item.player2, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${item.player2?.firstName ?: ""} ${item.player2?.lastName ?: ""}".trim(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "@${item.player2?.username ?: "Gracz 2"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentedButtonRow(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            onClick = { onTabSelected(0) },
            selected = selectedTabIndex == 0
        ) { Text("Statystyki") }
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            onClick = { onTabSelected(1) },
            selected = selectedTabIndex == 1
        ) { Text("Historia wbić") }
    }
}

@Composable
fun DetailsTab(
    matchStats: AggregatedStats?,
    frameDetails: Map<Int, AggregatedStats>,
    frames: List<Frame>,
    player1: User?,
    player2: User?
) {
    var selectedFrameIndex by remember { mutableStateOf(-1) } // -1 for "Cały mecz"

    Column(modifier = Modifier.fillMaxSize()) {
        DetailScopeTabs(
            frames = frames,
            selectedIndex = selectedFrameIndex,
            onFrameSelected = { selectedFrameIndex = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        val stats = if (selectedFrameIndex == -1) {
            matchStats
        } else {
            frameDetails[frames[selectedFrameIndex].frameNumber]
        }

        val title = if (selectedFrameIndex == -1) "Statystyki całego meczu" else "Statystyki frejma ${frames[selectedFrameIndex].frameNumber}"

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                StatsCard(
                    stats = stats,
                    title = title,
                    player1 = player1,
                    player2 = player2
                )
            }
            if (stats != null && (stats.player1Breaks.any { it.value >= 20 } || stats.player2Breaks.any { it.value >= 20 })) {
                item {
                    val p1Breaks = stats.player1Breaks.filter { it.value >= 20 }.sortedByDescending { it.value }
                    val p2Breaks = stats.player2Breaks.filter { it.value >= 20 }.sortedByDescending { it.value }
                    if (p1Breaks.isNotEmpty() || p2Breaks.isNotEmpty()) {
                        BreaksList(
                            player1Breaks = p1Breaks.map { it.value },
                            player2Breaks = p2Breaks.map { it.value },
                            player1Name = player1?.firstName ?: "Gracz 1",
                            player2Name = player2?.firstName ?: "Gracz 2"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryTab(
    matchItem: MatchHistoryDisplayItem,
    frameHistories: Map<Int, List<FrameShotHistory>>
) {
    var selectedFrame by remember { mutableStateOf(matchItem.match.frames.firstOrNull()) }

    Column(modifier = Modifier.fillMaxSize()) {
        FrameTabs(
            frames = matchItem.match.frames,
            selectedFrame = selectedFrame,
            onFrameSelected = { selectedFrame = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        val history = frameHistories[selectedFrame?.frameNumber] ?: emptyList()

        FrameHistorySubTab(
            frame = selectedFrame,
            history = history,
            p1Name = matchItem.player1?.firstName,
            p2Name = matchItem.player2?.firstName,
            player1Id = matchItem.match.player1Id
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BreaksList(
    player1Breaks: List<Int>,
    player2Breaks: List<Int>,
    player1Name: String,
    player2Name: String
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Brejki 20+",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = player1Name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        player1Breaks.forEach {
                            AssistChip(onClick = {}, label = { Text(it.toString()) })
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = player2Name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        player2Breaks.forEach {
                            AssistChip(onClick = {}, label = { Text(it.toString()) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreaksInfoDialog(
    breaks: List<Break>,
    playerName: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Podejścia punktowe - $playerName",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(breaks) { breakEntry ->
                        val groupedBalls = breakEntry.balls
                            .groupBy { it }
                            .map { (ball, list) -> ball to list.size }
                            .sortedBy { (ball, _) -> ball.points }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                modifier = Modifier.width(90.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                text = buildAnnotatedString {
                                    append("F${breakEntry.frameNumber}: ")
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("${breakEntry.value}")
                                    }
                                    append(" pkt:")
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(groupedBalls) { (ball, count) ->
                                    ShotBallIcon(ball, count)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Zamknij")
                }
            }
        }
    }
}

@Composable
fun StatsCard(stats: AggregatedStats?, title: String, player1: User?, player2: User?) {
    var showPlayer1BreaksDialog by remember { mutableStateOf(false) }
    var showPlayer2BreaksDialog by remember { mutableStateOf(false) }

    if (showPlayer1BreaksDialog) {
        BreaksInfoDialog(
            breaks = stats?.player1Breaks ?: emptyList(),
            playerName = player1?.firstName ?: "Gracz 1",
            onDismiss = { showPlayer1BreaksDialog = false }
        )
    }

    if (showPlayer2BreaksDialog) {
        BreaksInfoDialog(
            breaks = stats?.player2Breaks ?: emptyList(),
            playerName = player2?.firstName ?: "Gracz 2",
            onDismiss = { showPlayer2BreaksDialog = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            if (stats == null) {
                Text("Brak statystyk do wyświetlenia.", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                val totalPoints = (stats.player1TotalPoints + stats.player2TotalPoints).coerceAtLeast(1)
                val p1PointsRatio = stats.player1TotalPoints.toFloat() / totalPoints

                val totalHighestBreak = (stats.player1HighestBreak + stats.player2HighestBreak).coerceAtLeast(1)
                val p1BreakRatio = stats.player1HighestBreak.toFloat() / totalHighestBreak

                val totalTimeAtTable = (stats.player1ShotTotalTime + stats.player2ShotTotalTime).coerceAtLeast(1)
                val p1TimeRatio = stats.player1ShotTotalTime.toFloat() / totalTimeAtTable

                val totalFoulPoints = (stats.player1FoulPointsGiven + stats.player2FoulPointsGiven).coerceAtLeast(1)

                if (stats.winnerId != null) {
                    val p1FrameScore = if (stats.winnerId == player1?.uid) 1 else 0
                    val p2FrameScore = if (stats.winnerId == player2?.uid) 1 else 0
                    Spacer(modifier = Modifier.height(16.dp))
                    StatsRow(
                        label = "Zwycięzca",
                        player1Content = { Text(p1FrameScore.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold) },
                        player2Content = { Text(p2FrameScore.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Points
                AnimatedComparisonBar(
                    label = "Punkty",
                    value1 = stats.player1TotalPoints,
                    value2 = stats.player2TotalPoints,
                    ratio1 = p1PointsRatio,
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Highest Break
                AnimatedComparisonBar(
                    label = "Najwyższy brejk",
                    value1 = stats.player1HighestBreak,
                    value2 = stats.player2HighestBreak,
                    ratio1 = p1BreakRatio
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Time at table
                AnimatedComparisonBar(
                    label = "Czas przy stole",
                    value1 = stats.player1ShotTotalTime.toInt(),
                    value2 = stats.player2ShotTotalTime.toInt(),
                    ratio1 = p1TimeRatio,
                    isTime = true
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Fouls
                AnimatedComparisonBar(
                    label = "Punkty po faulach",
                    value1 = stats.player2FoulPointsGiven, // p1 gets points from p2's fouls
                    value2 = stats.player1FoulPointsGiven, // p2 gets points from p1's fouls
                    ratio1 = if(totalFoulPoints > 0) stats.player2FoulPointsGiven.toFloat() / totalFoulPoints else 0.5f,
                    subValue1 = "(${stats.player2Fouls} faule)",
                    subValue2 = "(${stats.player1Fouls} faule)"
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Other stats
                StatsRow(
                    label = "Skuteczność wbić",
                    player1Content = {
                        Text(text = "${String.format("%.1f", stats.player1PotSuccess)}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(text = "(${stats.player1Pots}/${stats.player1Pots + stats.player1Misses})", style = MaterialTheme.typography.bodySmall)
                    },
                    player2Content = {
                        Text(text = "${String.format("%.1f", stats.player2PotSuccess)}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(text = "(${stats.player2Pots}/${stats.player2Pots + stats.player2Misses})", style = MaterialTheme.typography.bodySmall)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                StatsRow(
                    label = "Skuteczność odstawnych",
                    player1Content = {
                        Text(text = "${String.format("%.1f", stats.player1SafetySuccess)}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(text = "(${stats.player1SafetySuccessCount}/${stats.player1Safeties})", style = MaterialTheme.typography.bodySmall)
                    },
                    player2Content = {
                        Text(text = "${String.format("%.1f", stats.player2SafetySuccess)}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(text = "(${stats.player2SafetySuccessCount}/${stats.player2Safeties})", style = MaterialTheme.typography.bodySmall)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                StatsRow(
                    label = "Średnie punktowanie",
                    player1Content = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showPlayer1BreaksDialog = true }
                        ) {
                            Text(String.format("%.1f", stats.player1AverageBreak), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    },
                    player2Content = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showPlayer2BreaksDialog = true }
                        ) {
                            Text(String.format("%.1f", stats.player2AverageBreak), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                StatsRow(
                    label = "Śr. czas uderzenia",
                    player1Content = { Text("${String.format("%.1f", stats.player1AverageShotTime)} s", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold) },
                    player2Content = { Text("${String.format("%.1f", stats.player2AverageShotTime)} s", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold) }
                )
            }
        }
    }
}

@Composable
fun AnimatedComparisonBar(
    label: String,
    value1: Int,
    value2: Int,
    ratio1: Float,
    isTime: Boolean = false,
    subValue1: String? = null,
    subValue2: String? = null
) {
    val animatedRatio1 by animateFloatAsState(targetValue = ratio1, label = "ratio1")

    val color1 = MaterialTheme.colorScheme.primary
    val color2 = MaterialTheme.colorScheme.primaryContainer

    val displayValue1 = if (isTime) formatMillisToTime(value1.toLong()) else value1.toString()
    val displayValue2 = if (isTime) formatMillisToTime(value2.toLong()) else value2.toString()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(displayValue1, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (subValue1 != null) Text(subValue1, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                Row(modifier = Modifier.clip(RoundedCornerShape(6.dp))) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(animatedRatio1).background(color1))
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(1f).background(color2))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(displayValue2, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (subValue2 != null) Text(subValue2, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun formatMillisToTime(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
private fun StatsRow(
    label: String,
    player1Content: @Composable ColumnScope.() -> Unit,
    player2Content: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { player1Content() }
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { player2Content() }
    }
}

@Composable
private fun DetailScopeTabs(
    frames: List<Frame>,
    selectedIndex: Int,
    onFrameSelected: (Int) -> Unit
) {
    val items = listOf("Cały mecz") + frames.map { "FREJM ${it.frameNumber}" }
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        items(items.size) { index ->
            val title = items[index]
            val adjustedIndex = index - 1
            FilterChip(
                selected = selectedIndex == adjustedIndex,
                onClick = { onFrameSelected(adjustedIndex) },
                label = { Text(title) }
            )
        }
    }
}

@Composable
private fun FrameTabs(
    frames: List<Frame>,
    selectedFrame: Frame?,
    onFrameSelected: (Frame) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        items(frames) { frame ->
            FilterChip(
                selected = frame == selectedFrame,
                onClick = { onFrameSelected(frame) },
                label = { Text("FREJM ${frame.frameNumber}") }
            )
        }
    }
}

@Composable
private fun FrameHistorySubTab(
    frame: Frame?,
    history: List<FrameShotHistory>,
    p1Name: String?,
    p2Name: String?,
    player1Id: String
) {
    if (frame == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Wybierz frejm, aby zobaczyć historię")
        }
        return
    }
    var showSafetyShots by remember { mutableStateOf(false) }
    val filteredHistory = if (showSafetyShots) {
        history
    } else {
        history.filter { it.shot.points > 0 || it.shot.type == ShotType.FOUL }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("${frame.player1Points} - ${frame.player2Points}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))
        FrameHistoryChart(history = history, player1Id = player1Id)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pokaż odstawne",
                style = MaterialTheme.typography.bodySmall
            )
            Checkbox(
                checked = showSafetyShots,
                onCheckedChange = { showSafetyShots = it }
            )
        }

        LazyColumn {
            items(filteredHistory) { historyItem ->
                HistoryRow(historyItem, p1Name, p2Name, player1Id)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
fun FrameHistoryChart(history: List<FrameShotHistory>, player1Id: String) {
    if (history.isEmpty()) {
        Box(modifier = Modifier.height(100.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Brak danych do wyświetlenia wykresu", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    val scoreDifferences = remember(history, player1Id) {
        val diffs = mutableListOf(0)
        history.forEach { diffs.add(it.player1ScoreAfter - it.player2ScoreAfter) }
        diffs
    }

    val maxAbsDifference = remember(scoreDifferences) {
        scoreDifferences.maxOfOrNull { abs(it) } ?: 1
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp).padding(vertical = 8.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val middleY = canvasHeight / 2f

        // Draw zero line
        drawLine(
            start = Offset(x = 0f, y = middleY),
            end = Offset(x = canvasWidth, y = middleY),
            color = Color.Gray,
            strokeWidth = 1.dp.toPx()
        )

        val linePath = Path()
        linePath.moveTo(0f, middleY)

        val fillPath = Path()
        fillPath.moveTo(0f, middleY)

        scoreDifferences.forEachIndexed { index, diff ->
            val x = (index.toFloat() / (scoreDifferences.size - 1).toFloat()) * canvasWidth
            val y = middleY - (diff.toFloat() / maxAbsDifference.toFloat()) * middleY
            linePath.lineTo(x, y)
            fillPath.lineTo(x, y)
        }

        // Close the fill path to the zero line for a clean fill area
        val lastX = ((scoreDifferences.size - 1).toFloat() / (scoreDifferences.size - 1).toFloat()) * canvasWidth
        fillPath.lineTo(lastX, middleY)
        fillPath.close()

        // Draw the gradient fill for the area under/over the line
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color.Green.copy(alpha = 0.3f), Color.Red.copy(alpha = 0.3f)),
                startY = 0f,
                endY = canvasHeight
            )
        )

        // Draw the score progression line with a gradient
        drawPath(
            path = linePath,
            brush = Brush.verticalGradient(
                colors = listOf(Color.Green, Color.Red),
                startY = 0f,
                endY = canvasHeight
            ),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}


@Composable
private fun HistoryRow(item: FrameShotHistory, p1Name: String?, p2Name: String?, player1Id: String) {
    val isFoul = item.shot.type == ShotType.FOUL
    val pointsText = when {
        isFoul -> "+${item.shot.points} F"
        item.shot.points == 0 -> "Odstawna"
        else -> "+${item.shot.points} (${item.breakValueAfter})"
    }

    val isPlayer1Beneficiary = (isFoul && item.activePlayerId != player1Id) || (!isFoul && item.activePlayerId == player1Id)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player 1 side
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            if (isPlayer1Beneficiary) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(pointsText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    when {
                        isFoul -> FoulIcon()
                        item.shot.points > 0 -> ShotBallIcon(SnookerBall.fromName(item.shot.ballName))
                        else -> SafetyIcon()
                    }
                }
            }
        }

        // Center Score
        Text(
            "${item.player1ScoreAfter} - ${item.player2ScoreAfter}",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Player 2 side
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (!isPlayer1Beneficiary) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when {
                        isFoul -> FoulIcon()
                        item.shot.points > 0 -> ShotBallIcon(SnookerBall.fromName(item.shot.ballName))
                        else -> SafetyIcon()
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(pointsText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FoulIcon() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.error),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "F",
            color = MaterialTheme.colorScheme.onError,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SafetyIcon() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun ShotBallIcon(ball: SnookerBall?, count: Int = 1) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(ball?.color ?: Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (count > 0) {
            Text(
                text = count.toString(),
                color = ball?.contentColor ?: Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

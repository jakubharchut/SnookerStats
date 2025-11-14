package com.example.snookerstats.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.snookerstats.domain.model.Frame
import com.example.snookerstats.domain.model.ShotType
import com.example.snookerstats.domain.model.SnookerBall
import com.example.snookerstats.ui.common.UserAvatar
import java.text.SimpleDateFormat
import java.util.*
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
                            frames = matchItem.match.frames
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
        ) { Text("Szczegóły") }
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            onClick = { onTabSelected(1) },
            selected = selectedTabIndex == 1
        ) { Text("Historia Meczu") }
    }
}

@Composable
fun DetailsTab(
    matchStats: AggregatedStats?,
    frameDetails: Map<Int, AggregatedStats>,
    frames: List<Frame>
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

        StatsDisplay(stats = stats, title = title)
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

@Composable
fun StatsDisplay(stats: AggregatedStats?, title: String) {
    if (stats == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Brak statystyk do wyświetlenia")
        }
        return
    }
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        val duration = stats.durationMillis
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60

        StatsRow("Punkty", stats.player1TotalPoints.toString(), stats.player2TotalPoints.toString())
        Divider()
        StatsRow("Najwyższy brejk", stats.player1HighestBreak.toString(), stats.player2HighestBreak.toString())
        Divider()
        StatsRow("Średnia brejka", String.format("%.2f", stats.player1AverageBreak), String.format("%.2f", stats.player2AverageBreak))
        Divider()
        StatsRow("Faule", stats.player1Fouls.toString(), stats.player2Fouls.toString())
        Divider()
        if (stats.durationMillis > 0) {
            StatsRow("Czas trwania", String.format("%02d:%02d", minutes, seconds), "")
        }
    }
}

@Composable
private fun StatsRow(label: String, player1Value: String, player2Value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(player1Value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
        Text(player2Value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
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
                Divider()
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
        history.forEach { 
            diffs.add(it.player1ScoreAfter - it.player2ScoreAfter)
        }
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
            strokeWidth = 2f
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

        // Close the fill path
        val lastX = canvasWidth
        fillPath.lineTo(lastX, middleY)
        fillPath.close()

        // Draw the gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color.Green.copy(alpha = 0.3f), Color.Red.copy(alpha = 0.3f)),
                startY = 0f,
                endY = canvasHeight
            )
        )
        
        // Draw the line
        drawPath(
            path = linePath,
            brush = Brush.verticalGradient(
                colors = listOf(Color.Green, Color.Red),
                startY = 0f,
                endY = canvasHeight
            ),
            style = Stroke(width = 4f)
        )
    }
}

@Composable
private fun HistoryRow(item: FrameShotHistory, p1Name: String?, p2Name: String?, player1Id: String) {
    val ball = SnookerBall.fromName(item.shot.ballName)
    val pointsText: String
    val showBallIcon: Boolean

    when(item.shot.type) {
        ShotType.FOUL -> {
            pointsText = "+${item.shot.points} F"
            showBallIcon = true
        }
        else -> { // Covers all other ShotType values
            if (item.shot.points == 0) {
                pointsText = "Odstawna"
                showBallIcon = false // Do not show ball for safety/missed 0-point shots
            } else {
                pointsText = "+${item.shot.points} (${item.breakValueAfter})"
                showBallIcon = true // Show ball for scoring shots
            }
        }
    }
    
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        if (item.activePlayerId == player1Id) {
            Text(pointsText, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Spacer(modifier = Modifier.width(8.dp))
            if (showBallIcon) {
                ShotBallIcon(ball)
            } else {
                Spacer(modifier = Modifier.size(16.dp)) // Match the size of ShotBallIcon for alignment
            }
        } else {
            // Inactive player side: always a spacer for the ball icon, to maintain symmetric layout
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            Spacer(modifier = Modifier.size(16.dp))
        }

        Text(
            "${item.player1ScoreAfter} - ${item.player2ScoreAfter}",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontWeight = FontWeight.Bold
        )

        if (item.activePlayerId != player1Id) {
            if (showBallIcon) {
                ShotBallIcon(ball)
            } else {
                Spacer(modifier = Modifier.size(16.dp)) // Match the size of ShotBallIcon for alignment
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(pointsText, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
        } else {
            // Inactive player side: always a spacer for the ball icon, to maintain symmetric layout
            Spacer(modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ShotBallIcon(ball: SnookerBall?) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(ball?.color ?: Color.Transparent)
    )
}

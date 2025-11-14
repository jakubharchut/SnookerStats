package com.example.snookerstats.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
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
    var selectedTab by remember { mutableStateOf(1) }

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
                        MatchDetailsTab(matchItem = matchItem)
                    } else {
                        MatchHistoryTab(
                            matchItem = matchItem,
                            frameHistories = uiState.frameHistories,
                            frameDetails = uiState.frameDetails
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
fun MatchDetailsTab(matchItem: MatchHistoryDisplayItem) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Ogólne statystyki meczu (w budowie)")
    }
}

@Composable
fun MatchHistoryTab(
    matchItem: MatchHistoryDisplayItem,
    frameHistories: Map<Int, List<FrameShotHistory>>,
    frameDetails: Map<Int, FrameStats>
) {
    var selectedFrame by remember { mutableStateOf(matchItem.match.frames.firstOrNull()) }
    var showSafetyShots by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        FrameTabs(
            frames = matchItem.match.frames,
            selectedFrame = selectedFrame,
            onFrameSelected = { selectedFrame = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        selectedFrame?.let { frame ->
            val history = frameHistories[frame.frameNumber] ?: emptyList()
            val stats = frameDetails[frame.frameNumber]
            FrameHistoryContent(
                frame = frame,
                history = history,
                stats = stats,
                p1Name = matchItem.player1?.firstName,
                p2Name = matchItem.player2?.firstName,
                player1Id = matchItem.match.player1Id,
                showSafetyShots = showSafetyShots,
                onShowSafetyShotsChange = { showSafetyShots = it }
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
private fun FrameHistoryContent(
    frame: Frame,
    history: List<FrameShotHistory>,
    stats: FrameStats?,
    p1Name: String?,
    p2Name: String?,
    player1Id: String,
    showSafetyShots: Boolean,
    onShowSafetyShotsChange: (Boolean) -> Unit
) {
    val filteredHistory = if (showSafetyShots) {
        history
    } else {
        history.filter { it.shot.points > 0 || it.shot.type == ShotType.FOUL }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("HB: ${stats?.player1HighestBreak ?: 0}", style = MaterialTheme.typography.bodyLarge)
            Text("${frame.player1Points} - ${frame.player2Points}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("HB: ${stats?.player2HighestBreak ?: 0}", style = MaterialTheme.typography.bodyLarge)
        }
        val duration = stats?.durationMillis ?: 0
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        Text(
            String.format("Czas trwania: %02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

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
                onCheckedChange = onShowSafetyShotsChange
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

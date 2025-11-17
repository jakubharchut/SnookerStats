package com.example.snookerstats.ui.screens.training

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.snookerstats.domain.model.SnookerBall
import com.example.snookerstats.domain.model.TrainingAttempt
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LineUpStatsScreen(viewModel: LineUpStatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedAttemptId by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            uiState.error != null -> Text(text = "Błąd: ${uiState.error}", modifier = Modifier.align(Alignment.Center))
            uiState.attemptsByDate.isEmpty() -> Text(text = "Brak zapisanych prób.", modifier = Modifier.align(Alignment.Center))
            else -> {
                LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                    item {
                        GlobalStatsPanel(stats = uiState.globalStats)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Historia prób:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    uiState.attemptsByDate.forEach { (date, attempts) ->
                        stickyHeader {
                            Text(
                                text = date,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                        items(attempts) { attempt ->
                            AttemptItem(
                                attempt = attempt,
                                isExpanded = expandedAttemptId == attempt.id,
                                averageScore = uiState.globalStats.averageScore,
                                onClick = {
                                    expandedAttemptId = if (expandedAttemptId == attempt.id) null else attempt.id
                                }
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttemptItem(
    attempt: TrainingAttempt,
    isExpanded: Boolean,
    averageScore: Double,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp).clickable(onClick = onClick)) {
        TrainingStats(
            score = attempt.score,
            pottedBallsCount = attempt.pottedBalls.size,
            time = attempt.durationInSeconds
        )
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val pottedBalls = attempt.pottedBalls.mapNotNull { SnookerBall.fromName(it) }
                PottedBallsHistory(pottedBalls = pottedBalls)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    val avgShotTime = if (attempt.pottedBalls.isNotEmpty()) attempt.durationInSeconds.toDouble() / attempt.pottedBalls.size else 0.0
                    StatItem(label = "Śr. czas uderzenia", value = "%.1fs".format(avgShotTime))
                    CompareToAverage(score = attempt.score, average = averageScore)
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CompareToAverage(score: Int, average: Double) {
    val difference = score - average
    val (icon, color, text) = when {
        difference > 0 -> Triple(Icons.Default.ArrowUpward, Color(0xFF4CAF50), "%.1f pkt ponad średnią".format(difference))
        difference < 0 -> Triple(Icons.Default.ArrowDownward, MaterialTheme.colorScheme.error, "%.1f pkt poniżej średniej".format(kotlin.math.abs(difference)))
        else -> Triple(Icons.Default.TrendingFlat, MaterialTheme.colorScheme.onSurface, "Wynik równy średniej")
    }
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.padding(start = 4.dp))
        Text(text = text, color = color, style = MaterialTheme.typography.bodyMedium)
    }
}

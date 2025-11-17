package com.example.snookerstats.ui.screens.training

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.snookerstats.domain.model.SnookerBall
import com.example.snookerstats.domain.model.TrainingAttempt
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LineUpStatsScreen(viewModel: LineUpStatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedAttemptId by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            uiState.error != null -> Text(text = "Błąd: ${uiState.error}", modifier = Modifier.align(Alignment.Center))
            uiState.attempts.isEmpty() -> Text(text = "Brak zapisanych prób.", modifier = Modifier.align(Alignment.Center))
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TrainingStats(
                        score = uiState.globalStats.bestScore,
                        pottedBallsCount = uiState.globalStats.attemptCount,
                        time = uiState.globalStats.bestTimeInSeconds,
                        isGlobal = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Historia prób:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                        items(uiState.attempts) { attempt ->
                            AttemptItem(
                                attempt = attempt,
                                isExpanded = expandedAttemptId == attempt.id,
                                onClick = {
                                    expandedAttemptId = if (expandedAttemptId == attempt.id) null else attempt.id
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
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
    onClick: () -> Unit
) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        TrainingStats(
            score = attempt.score,
            pottedBallsCount = attempt.pottedBalls.size,
            time = attempt.durationInSeconds
        )
        AnimatedVisibility(visible = isExpanded) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(8.dp))
                val pottedBalls = attempt.pottedBalls.mapNotNull { SnookerBall.fromName(it) }
                PottedBallsHistory(pottedBalls = pottedBalls)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(attempt.date!!),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
    }
}

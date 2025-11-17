package com.example.snookerstats.ui.screens.training

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.snookerstats.domain.model.SnookerBall
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LineUpStatsScreen(viewModel: LineUpStatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            uiState.error != null -> Text(text = "Błąd: ${uiState.error}", modifier = Modifier.align(Alignment.Center))
            uiState.attempts.isEmpty() -> Text(text = "Brak zapisanych prób.", modifier = Modifier.align(Alignment.Center))
            else -> {
                LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                    items(uiState.attempts) { attempt ->
                        val pottedBalls = attempt.pottedBalls.mapNotNull { SnookerBall.fromName(it) }
                        TrainingStats(
                            score = attempt.score,
                            pottedBallsCount = pottedBalls.size,
                            time = attempt.durationInSeconds
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PottedBallsHistory(pottedBalls = pottedBalls)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(attempt.date!!),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

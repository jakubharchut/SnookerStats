package com.example.snookerstats.ui.screens.training

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RedBlackStatsScreen(viewModel: RedBlackStatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            uiState.error != null -> Text(text = "Błąd: ${uiState.error}", modifier = Modifier.align(Alignment.Center))
            uiState.attemptsByDate.isEmpty() -> Text(text = "Brak zapisanych prób.", modifier = Modifier.align(Alignment.Center))
            else -> {
                LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                    item {
                        RedBlackGlobalStatsPanel(
                            stats = uiState.globalStats
                        )
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
                            TrainingStats(
                                score = attempt.score,
                                pottedBallsCount = attempt.pottedBalls.size,
                                time = attempt.durationInSeconds
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

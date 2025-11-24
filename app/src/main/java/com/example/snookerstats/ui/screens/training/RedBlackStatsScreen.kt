package com.example.snookerstats.ui.screens.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.snookerstats.domain.model.SnookerBall

@Composable
fun RedBlackStatsScreen(
    viewModel: RedBlackStatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading && uiState.attemptsByDate.isEmpty()) { // Show loading only on initial load
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Błąd: ${uiState.error}")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp)) // Padding top
                FilterChips(selectedFilter = uiState.selectedFilter, onFilterSelected = viewModel::onFilterSelected)
            }
            item {
                PerformanceHistoryChart(
                    dailyAverageHistory = uiState.globalStats.dailyAverageHistory,
                    overallAverage = uiState.globalStats.averageStreak
                )
            }
            item {
                RedBlackGlobalStatsPanel(stats = uiState.globalStats)
            }
            item {
                RedBlackPottingStatsPanel(stats = uiState.globalStats)
            }

            uiState.attemptsByDate.forEach { (date, attempts) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(attempts) { attempt ->
                    AttemptItem(attempt = attempt)
                }
            }
        }
    }
}

@Composable
private fun RedBlackGlobalStatsPanel(stats: RedBlackGlobalStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Najw. passa", style = MaterialTheme.typography.labelMedium)
                Text(stats.bestStreak.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Śr. passa", style = MaterialTheme.typography.labelMedium)
                Text(String.format("%.1f", stats.averageStreak), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Liczba prób", style = MaterialTheme.typography.labelMedium)
                Text(stats.attemptCount.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RedBlackPottingStatsPanel(stats: RedBlackGlobalStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Skuteczność wbijania", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            val ballsInOrder = listOf(SnookerBall.Red, SnookerBall.Black)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ballsInOrder.forEach { ball ->
                    BallStatItem(ball = ball, stats = stats.ballStats[ball])
                }
            }
        }
    }
}
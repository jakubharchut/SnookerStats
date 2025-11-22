package com.example.snookerstats.ui.screens.training

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.snookerstats.domain.model.SnookerBall
import com.example.snookerstats.domain.model.TrainingAttempt

@Composable
fun LineUpStatsScreen(
    viewModel: LineUpStatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PerformanceHistoryChart(scoreHistory = uiState.globalStats.scoreHistory)
            }
            item {
                LineUpGlobalStatsPanel(stats = uiState.globalStats)
            }
            item {
                PottingStatsPanel(stats = uiState.globalStats)
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
                    LineUpAttemptItem(attempt = attempt)
                }
            }
        }
    }
}

@Composable
private fun PerformanceHistoryChart(scoreHistory: List<Int>) {
    if (scoreHistory.isEmpty()) {
        Box(modifier = Modifier.height(150.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Zagraj kilka treningów, aby zobaczyć wykres postępów", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    val maxScore = remember(scoreHistory) { scoreHistory.maxOrNull() ?: 1 }
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Postępy w czasie", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 8.dp)) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val linePath = Path()
                linePath.moveTo(0f, canvasHeight)

                scoreHistory.forEachIndexed { index, score ->
                    val x = (index.toFloat() / (scoreHistory.size - 1).toFloat()) * canvasWidth
                    val y = canvasHeight - (score.toFloat() / maxScore.toFloat()) * canvasHeight
                    linePath.lineTo(x, y)
                }

                drawPath(
                    path = linePath,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun LineUpGlobalStatsPanel(stats: LineUpGlobalStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Najw. brejk", style = MaterialTheme.typography.labelMedium)
                Text(stats.bestScore.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Śr. brejk", style = MaterialTheme.typography.labelMedium)
                Text(String.format("%.1f", stats.averageScore), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Liczba prób", style = MaterialTheme.typography.labelMedium)
                Text(stats.attemptCount.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LineUpAttemptItem(attempt: TrainingAttempt) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Wynik: ${attempt.score}", fontWeight = FontWeight.Bold)
            Text("Czas: ${attempt.durationInSeconds}s")
        }
    }
}

@Composable
private fun PottingStatsPanel(stats: LineUpGlobalStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Skuteczność wbijania", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            val ballsInOrder = listOf(SnookerBall.Red, SnookerBall.Yellow, SnookerBall.Green, SnookerBall.Brown, SnookerBall.Blue, SnookerBall.Pink, SnookerBall.Black)

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

@Composable
private fun BallStatItem(ball: SnookerBall, stats: BallStats?) {
    val pots = stats?.pots ?: 0
    val misses = stats?.misses ?: 0
    val total = pots + misses
    val accuracy = if (total > 0) (pots.toFloat() / total) * 100 else 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(ball.color, CircleShape)
        )
        Text(
            text = "%.0f%%".format(accuracy),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "($pots/$total)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
package com.example.snookerstats.ui.screens.training

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.snookerstats.domain.model.SnookerBall
import com.example.snookerstats.domain.model.TrainingAttempt
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun FilterChips(selectedFilter: FilterPeriod, onFilterSelected: (FilterPeriod) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
        FilterPeriod.values().forEach { period ->
            FilterChip(
                selected = selectedFilter == period,
                onClick = { onFilterSelected(period) },
                label = { Text(period.displayName, softWrap = false) }
            )
        }
    }
}

@Composable
fun PerformanceHistoryChart(
    dailyAverageHistory: List<Pair<Date, Double>>,
    overallAverage: Double
) {
    if (dailyAverageHistory.isEmpty()) {
        Box(modifier = Modifier.height(150.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Zagraj kilka treningów, aby zobaczyć wykres postępów", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    val scores = dailyAverageHistory.map { it.second.toFloat() }
    val yValues = scores + overallAverage.toFloat()
    val maxScore = remember(scores, overallAverage) { (yValues.maxOrNull() ?: 1f) * 1.1f }
    val minScore = remember(scores, overallAverage) { (yValues.minOrNull() ?: 0f) * 0.9f }
    val scoreRange = (maxScore - minScore).coerceAtLeast(1f)

    val greenColor = Color(0xFF4CAF50)
    val redColor = Color(0xFFF44336)
    val avgLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Średnia dzienna vs Średnia ogólna (${String.format("%.1f", overallAverage)})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Column(
                    modifier = Modifier.height(150.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(maxScore.roundToInt().toString(), style = MaterialTheme.typography.labelSmall)
                    Text(minScore.roundToInt().toString(), style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val avgY = canvasHeight - ((overallAverage.toFloat() - minScore) / scoreRange) * canvasHeight

                    val linePath = Path()
                    val fillPath = Path()

                    if (scores.isNotEmpty()) {
                        val firstX = 0f
                        val firstY = canvasHeight - ((scores.first() - minScore) / scoreRange) * canvasHeight
                        linePath.moveTo(firstX, firstY)
                        fillPath.moveTo(firstX, avgY)
                        fillPath.lineTo(firstX, firstY)
                    }

                    scores.forEachIndexed { index, score ->
                        val x = (index.toFloat() / (scores.size - 1).coerceAtLeast(1).toFloat()) * canvasWidth
                        val y = canvasHeight - ((score - minScore) / scoreRange) * canvasHeight
                        linePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }

                    val lastX = canvasWidth
                    fillPath.lineTo(lastX, avgY)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(greenColor.copy(alpha = 0.3f), redColor.copy(alpha = 0.3f)),
                            startY = 0f,
                            endY = canvasHeight
                        )
                    )

                    drawPath(
                        path = linePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(greenColor, redColor),
                            startY = 0f,
                            endY = canvasHeight
                        ),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    if (avgY in 0f..canvasHeight) {
                        drawLine(
                            start = Offset(x = 0f, y = avgY),
                            end = Offset(x = canvasWidth, y = avgY),
                            color = avgLineColor,
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttemptItem(attempt: TrainingAttempt) {
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
fun BallStatItem(ball: SnookerBall, stats: BallStats?) {
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

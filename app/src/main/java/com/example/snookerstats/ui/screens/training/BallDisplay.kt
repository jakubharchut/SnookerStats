package com.example.snookerstats.ui.screens.training

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.snookerstats.domain.model.SnookerBall

@Composable
fun RemainingBallsVisualizer(balls: List<SnookerBall>) {
    val redCount = balls.count { it is SnookerBall.Red }
    val colors = listOf(SnookerBall.Yellow, SnookerBall.Green, SnookerBall.Brown, SnookerBall.Blue, SnookerBall.Pink, SnookerBall.Black)
    val onTableColors = balls.filter { it !is SnookerBall.Red }.toSet()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Pozostało:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (redCount > 0) {
                BallIcon(ball = SnookerBall.Red, count = redCount)
            }
            colors.forEach { ball ->
                if (ball in onTableColors) {
                    BallIcon(ball = ball)
                }
            }
        }
    }
}

@Composable
internal fun BallIcon(ball: SnookerBall, count: Int? = null) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(ball.color)
            .border(1.dp, Color.Black, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (count != null) {
            Text(
                text = count.toString(),
                color = ball.contentColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

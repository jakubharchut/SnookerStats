package com.example.snookerstats.ui.screens.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.snookerstats.domain.model.SnookerBall

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PottedBallsHistory(pottedBalls: List<SnookerBall>) {
    val groupedBalls = pottedBalls.groupingBy { it }.eachCount()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Wbite w podejściu: (${pottedBalls.size})", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groupedBalls.forEach { (ball, count) ->
                BallIcon(ball = ball, count = count)
            }
        }
    }
}

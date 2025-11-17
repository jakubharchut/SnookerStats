package com.example.snookerstats.ui.screens.training

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.snookerstats.domain.model.SnookerBall

@Composable
fun BallsInfo(
    pottedBalls: List<SnookerBall>,
    remainingBalls: List<SnookerBall>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PottedBallsHistory(pottedBalls = pottedBalls)
            Spacer(modifier = Modifier.height(16.dp))
            RemainingBallsVisualizer(balls = remainingBalls)
        }
    }
}

package com.example.snookerstats.ui.screens.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.snookerstats.domain.model.SnookerBall

@Composable
fun PottingButtons(
    pottingColor: Boolean,
    finalSequence: Boolean,
    nextBall: SnookerBall?,
    onBallClick: (SnookerBall) -> Unit
) {
    val colors = listOf(SnookerBall.Yellow, SnookerBall.Green, SnookerBall.Brown, SnookerBall.Blue, SnookerBall.Pink, SnookerBall.Black)

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { onBallClick(SnookerBall.Red) },
            enabled = !pottingColor && !finalSequence,
            colors = ButtonDefaults.buttonColors(containerColor = SnookerBall.Red.color),
            modifier = Modifier.widthIn(min = 240.dp).height(48.dp)
        ) {
            Text("Czerwona", color = SnookerBall.Red.contentColor)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            colors.subList(0, 3).forEach { ball ->
                Button(
                    onClick = { onBallClick(ball) },
                    enabled = pottingColor || (finalSequence && ball == nextBall),
                    colors = ButtonDefaults.buttonColors(containerColor = ball.color),
                    modifier = Modifier.padding(horizontal = 4.dp).widthIn(min = 100.dp).height(48.dp)
                ) {
                    Text(ball.displayName, color = ball.contentColor)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            colors.subList(3, 6).forEach { ball ->
                Button(
                    onClick = { onBallClick(ball) },
                    enabled = pottingColor || (finalSequence && ball == nextBall),
                    colors = ButtonDefaults.buttonColors(containerColor = ball.color),
                    modifier = Modifier.padding(horizontal = 4.dp).widthIn(min = 100.dp).height(48.dp)
                ) {
                    Text(ball.displayName, color = ball.contentColor)
                }
            }
        }
    }
}

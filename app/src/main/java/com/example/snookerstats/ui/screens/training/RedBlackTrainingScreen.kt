package com.example.snookerstats.ui.screens.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.snookerstats.domain.model.SnookerBall

@Composable
fun RedBlackTrainingScreen(viewModel: RedBlackViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            RedBlackStatsPanel(
                streak = uiState.streak,
                record = uiState.record,
                time = uiState.elapsedTimeInSeconds
            )

            RedBlackPottingButtons(
                isPottingBlack = uiState.isPottingBlack,
                onBallClick = viewModel::onBallPotted
            )

            OutlinedButton(
                onClick = { viewModel.onMiss() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Pudło / Zakończ")
            }
        }
    }
}

@Composable
private fun RedBlackStatsPanel(streak: Int, record: Int, time: Long) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(label = "Passa", value = streak.toString())
            StatItem(label = "Rekord", value = record.toString())
            StatItem(label = "Czas", value = formatTime(time))
        }
    }
}

@Composable
private fun RedBlackPottingButtons(isPottingBlack: Boolean, onBallClick: (Boolean) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { onBallClick(true) },
            enabled = isPottingBlack,
            modifier = Modifier.width(240.dp).height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SnookerBall.Black.color,
                contentColor = SnookerBall.Black.contentColor,
                disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                disabledContentColor = Color.White.copy(alpha = 0.7f)
            )
        ) {
            Text("Czarna")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onBallClick(false) },
                enabled = !isPottingBlack,
                modifier = Modifier.width(116.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SnookerBall.Red.color,
                    contentColor = SnookerBall.Red.contentColor,
                    disabledContainerColor = SnookerBall.Red.color.copy(alpha = 0.3f),
                    disabledContentColor = SnookerBall.Red.contentColor.copy(alpha = 0.7f)
                )
            ) {
                Text("Lewy")
            }
            Button(
                onClick = { onBallClick(false) },
                enabled = !isPottingBlack,
                modifier = Modifier.width(116.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SnookerBall.Red.color,
                    contentColor = SnookerBall.Red.contentColor,
                    disabledContainerColor = SnookerBall.Red.color.copy(alpha = 0.3f),
                    disabledContentColor = SnookerBall.Red.contentColor.copy(alpha = 0.7f)
                )
            ) {
                Text("Prawy")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

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
import androidx.compose.ui.text.style.TextAlign
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
                .padding(it)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RedBlackStatsPanel(
                    streak = uiState.streak,
                    record = uiState.record,
                    time = uiState.elapsedTimeInSeconds
                )
                Spacer(modifier = Modifier.height(16.dp))
                SessionStatsPanel(
                    totalPotsOnRed = uiState.totalPotsOnRed,
                    totalMissesOnRed = uiState.totalMissesOnRed,
                    totalPotsOnBlack = uiState.totalPotsOnBlack,
                    totalMissesOnBlack = uiState.totalMissesOnBlack
                )
            }

            RedBlackPottingButtons(
                isPottingBlack = uiState.isPottingBlack,
                onBallClick = viewModel::onBallPotted
            )

            OutlinedButton(
                onClick = { viewModel.onMiss() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Pudło / Zakończ")
            }
        }
    }
}

@Composable
private fun RedBlackStatsPanel(streak: Int, record: Int, time: Long) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(label = "Podejście", value = streak.toString())
            StatItem(label = "Rekord", value = record.toString())
            StatItem(label = "Czas", value = formatTime(time))
        }
    }
}

@Composable
private fun SessionStatsPanel(
    totalPotsOnRed: Int,
    totalMissesOnRed: Int,
    totalPotsOnBlack: Int,
    totalMissesOnBlack: Int
) {
    val redTotal = totalPotsOnRed + totalMissesOnRed
    val redAccuracy = if (redTotal > 0) {
        (totalPotsOnRed.toFloat() / redTotal) * 100
    } else {
        0f
    }
    val blackTotal = totalPotsOnBlack + totalMissesOnBlack
    val blackAccuracy = if (blackTotal > 0) {
        (totalPotsOnBlack.toFloat() / blackTotal) * 100
    } else {
        0f
    }

    val redValue = "%.0f%%\n(%d/%d)".format(redAccuracy, totalPotsOnRed, redTotal)
    val blackValue = "%.0f%%\n(%d/%d)".format(blackAccuracy, totalPotsOnBlack, blackTotal)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Statystyki Sesji", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(label = "Skuteczność (Czerwone)", value = redValue)
                StatItem(label = "Skuteczność (Czarna)", value = blackValue)
            }
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
                Text("Czerwona")
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
                Text("Czerwona")
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value, 
            style = MaterialTheme.typography.headlineSmall, 
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

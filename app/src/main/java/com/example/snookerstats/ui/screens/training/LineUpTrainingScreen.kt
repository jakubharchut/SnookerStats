package com.example.snookerstats.ui.screens.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.snookerstats.domain.model.SnookerBall

@Composable
fun LineUpTrainingScreen(
    navController: NavController,
    viewModel: LineUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TrainingStats(
            score = uiState.score,
            pottedBallsCount = uiState.pottedBalls.size,
            time = uiState.elapsedTimeInSeconds
        )

        if (uiState.isFinished) {
            Button(
                onClick = { viewModel.resetTraining() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Zacznij od nowa")
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BallsInfo(pottedBalls = uiState.pottedBalls, remainingBalls = uiState.ballsOnTable)
                Spacer(modifier = Modifier.height(16.dp))
                PottingButtons(
                    pottingColor = uiState.pottingColor,
                    finalSequence = uiState.finalSequenceBall != null,
                    nextBall = uiState.nextBallToPot,
                    onBallClick = viewModel::onTableBallClick
                )
                OutlinedButton(
                    onClick = { 
                        val missedBall = uiState.nextBallToPot ?: SnookerBall.Red // A sensible default
                        viewModel.onMiss(missedBall)
                     },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "Pudło / Zakończ")
                }
            }
        }
    }
}
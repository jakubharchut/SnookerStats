package com.example.snookerstats.ui.screens.training

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineUpTrainingScreen(
    navController: NavController,
    viewModel: LineUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trening - Czyszczenie Linii") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TrainingStats(
                score = uiState.score,
                pottedBallsCount = uiState.pottedBalls.size,
                time = uiState.elapsedTimeInSeconds
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.pottedBalls.isNotEmpty()) {
                PottedBallsHistory(pottedBalls = uiState.pottedBalls)
            }
            
            Spacer(modifier = Modifier.weight(1f))

            RemainingBallsVisualizer(balls = uiState.ballsOnTable)
            
            Spacer(modifier = Modifier.weight(1f))

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
                PottingButtons(
                    pottingColor = uiState.pottingColor,
                    finalSequence = uiState.finalSequenceBall != null,
                    nextBall = uiState.nextBallToPot,
                    onBallClick = viewModel::onTableBallClick
                )
                OutlinedButton(
                    onClick = { viewModel.onMiss() },
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

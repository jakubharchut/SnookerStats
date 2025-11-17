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
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TrainingStats(
                score = uiState.score,
                pottedBallsCount = uiState.pottedBalls.size,
                time = uiState.elapsedTimeInSeconds
            )

            BallsInfo(pottedBalls = uiState.pottedBalls, remainingBalls = uiState.ballsOnTable)

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
}

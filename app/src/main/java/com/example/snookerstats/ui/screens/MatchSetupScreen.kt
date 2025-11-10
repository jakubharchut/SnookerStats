package com.example.snookerstats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.snookerstats.ui.common.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSetupScreen(
    navController: NavController,
    viewModel: MatchSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Back Button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Konfiguracja Meczu", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 32.dp, bottom = 24.dp))

                // Opponent Info Section
                when (val opponent = uiState.opponentType) {
                    is OpponentType.LOADING -> CircularProgressIndicator()
                    is OpponentType.PLAYER -> {
                        UserAvatar(user = opponent.user, modifier = Modifier.size(80.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "${opponent.user.firstName} ${opponent.user.lastName}", style = MaterialTheme.typography.titleLarge)
                        Text(text = "@${opponent.user.username}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    is OpponentType.GUEST -> {
                        Text("Mecz z Gościem", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = uiState.guestName,
                            onValueChange = viewModel::onGuestNameChange,
                            label = { Text("Imię gościa") },
                            singleLine = true
                        )
                    }
                    is OpponentType.SOLO -> {
                        Text("Trening Solo", style = MaterialTheme.typography.titleLarge)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Match Type Section
                Text("Rodzaj Meczu", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        onClick = { viewModel.onMatchTypeChange(MatchType.SPARRING) },
                        selected = uiState.matchType == MatchType.SPARRING
                    ) { Text("Sparingowy") }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        onClick = { viewModel.onMatchTypeChange(MatchType.RANKING) },
                        selected = uiState.matchType == MatchType.RANKING
                    ) { Text("Rankingowy") }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Match Format Section
                Text("Format Meczu (liczba czerwonych)", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    MatchFormat.values().forEachIndexed { index, format ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = MatchFormat.values().size),
                            onClick = { viewModel.onMatchFormatChange(format) },
                            selected = uiState.matchFormat == format
                        ) { Text(format.reds.toString()) }
                    }
                }
            }

            Button(
                onClick = { navController.navigate("scoring/${uiState.matchFormat.reds}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = !(uiState.opponentType is OpponentType.GUEST && uiState.guestName.isBlank())
            ) {
                Text("Rozpocznij Mecz", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

package com.example.snookerstats.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.snookerstats.domain.model.Match
import com.example.snookerstats.domain.model.MatchStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Ekran Główny")
    }
}

@Composable
fun PlayScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Play Screen")
    }
}

@Composable
fun MatchHistoryScreen(
    viewModel: MatchHistoryViewModel = hiltViewModel()
) {
    val matches by viewModel.matches.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Historia Meczów",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        if (matches.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Brak rozegranych meczów.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(matches) { match ->
                    MatchHistoryItem(match = match)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun MatchHistoryItem(match: Match) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "ID Meczu: ${match.id}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Data: ${formatTimestamp(match.date)}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Gracz 1: ${match.player1Id}", style = MaterialTheme.typography.bodyMedium)
            match.player2Id?.let { 
                Text(text = "Gracz 2: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "Typ Meczu: ${match.matchType}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${match.status}", style = MaterialTheme.typography.bodyMedium)
            
            if (match.status == MatchStatus.COMPLETED) {
                val lastFrame = match.frames.lastOrNull()
                if (lastFrame != null) {
                    val p1Score = lastFrame.player1Points
                    val p2Score = lastFrame.player2Points
                    val winner = if (p1Score > p2Score) match.player1Id else match.player2Id
                    Text(text = "Wynik: $p1Score - $p2Score", style = MaterialTheme.typography.bodyLarge)
                    winner?.let { Text(text = "Zwycięzca: $it", style = MaterialTheme.typography.bodyLarge) }
                }
            }
        }
    }
}

@Composable
fun StatsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Stats Screen")
    }
}

// Usunięto zduplikowaną funkcję CommunityScreen()

// Te ekrany nie są już w dolnym menu, ale zostają na przyszłość
@Composable
fun TournamentsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Tournaments Screen")
    }
}

@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Profile Screen")
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

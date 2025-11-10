package com.example.snookerstats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ScoringScreen(navController: NavController) {
    // Na razie używamy przykładowych danych
    val player1Name = "koobi"
    val player2Name = "Merka"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Górny panel wyników
        Scoreboard(player1Name = player1Name, player2Name = player2Name, player1Score = 0, player2Score = 0)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 2. Panel statystyk bieżących (na razie placeholder)
        CurrentStats()

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        
        // 3. Panel akcji (przyciski)
        BallButtons()

        // 4. Panel akcji specjalnych
        ActionButtons()
    }
}

@Composable
private fun Scoreboard(player1Name: String, player2Name: String, player1Score: Int, player2Score: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = player1Name, style = MaterialTheme.typography.titleMedium)
                Text(text = player1Score.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            }
            Text(text = "vs", style = MaterialTheme.typography.titleLarge)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = player2Name, style = MaterialTheme.typography.titleMedium)
                Text(text = player2Score.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CurrentStats() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Break", fontSize = 12.sp)
            Text("0", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Pozostało", fontSize = 12.sp)
            Text("147", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Czerwonych", fontSize = 12.sp)
            Text("15", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BallButtons() {
    // Użyjemy FlowRow, aby przyciski same się zawijały
    // (na razie uproszczony Row)
    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Czerwona") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Żółta") }
            Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Zielona") }
            Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Brązowa") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Niebieska") }
            Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Różowa") }
            Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Czarna") }
        }
    }
}

@Composable
private fun ActionButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Faul") }
        Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Koniec podejścia") }
        Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Undo") }
    }
}

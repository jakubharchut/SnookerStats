package com.example.snookerstats.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.snookerstats.domain.model.SnookerBall

@Composable
fun ScoringScreen(navController: NavController) {
    // Na razie używamy przykładowych danych
    val player1FirstName = "Jakub"
    val player1LastName = "Kowalski"
    val player1Username = "koobi"
    val player1FramesWon = 1

    val player2FirstName = "Anna"
    val player2LastName = "Nowak"
    val player2Username = "Merka"
    val player2FramesWon = 0

    val sampleBreak = listOf(SnookerBall.Red, SnookerBall.Black, SnookerBall.Red)
    val sampleTime = "00:42"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Górny panel wyników
        Scoreboard(
            player1FirstName = player1FirstName,
            player1LastName = player1LastName,
            player1Username = player1Username,
            player1Score = 8,
            player1FramesWon = player1FramesWon,
            player2FirstName = player2FirstName,
            player2LastName = player2LastName,
            player2Username = player2Username,
            player2Score = 0,
            player2FramesWon = player2FramesWon
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 2. Panel statystyk bieżących i zegar
        CurrentStatsAndTimer(time = sampleTime)

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Wizualizacja brejka
        BreakVisualizer(breakBalls = sampleBreak)

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        
        // 4. Panel akcji (przyciski bil)
        BallButtons()

        Spacer(modifier = Modifier.height(16.dp))
        
        // 5. Panel akcji specjalnych
        ActionButtons()

        // Pusty element, który "wypycha" ostatnie przyciski na dół
        Spacer(modifier = Modifier.weight(1f))

        // 6. Akcje zakończenia
        FrameAndMatchActions()
    }
}

@Composable
private fun Scoreboard(
    player1FirstName: String, player1LastName: String, player1Username: String, player1Score: Int, player1FramesWon: Int,
    player2FirstName: String, player2LastName: String, player2Username: String, player2Score: Int, player2FramesWon: Int
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Player 1 Info
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(text = "$player1FirstName $player1LastName", style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(text = "@$player1Username", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = player1Score.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            }
            
            // Frame Score
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(text = "FRAMES", style = MaterialTheme.typography.labelSmall)
                Text(
                    text = "$player1FramesWon - $player2FramesWon",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Player 2 Info
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(text = "$player2FirstName $player2LastName", style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(text = "@$player2Username", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = player2Score.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CurrentStatsAndTimer(time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Break", fontSize = 12.sp)
            Text("8", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Pozostało", fontSize = 12.sp)
            Text("131", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Czerwonych", fontSize = 12.sp)
            Text("13", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Czas podejścia", fontSize = 12.sp)
            Text(time, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BreakVisualizer(breakBalls: List<SnookerBall>) {
    val groupedBalls = breakBalls.groupBy { it }.mapValues { it.value.size }.entries.sortedBy { it.key.points }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .padding(horizontal = 16.dp)
    ) {
        if (groupedBalls.isEmpty()) {
            Text(
                "Brak bil w brejku",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            groupedBalls.forEach { (ball, count) ->
                BallIcon(ball = ball, count = count)
            }
        }
    }
}

@Composable
private fun BallIcon(ball: SnookerBall, count: Int) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
        Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
            drawCircle(color = ball.color)
        })
        if (count > 0) {
            Text(
                text = count.toString(),
                color = ball.contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BallButtons() {
    val colors = listOf(
        SnookerBall.Yellow, SnookerBall.Green, SnookerBall.Brown,
        SnookerBall.Blue, SnookerBall.Pink, SnookerBall.Black
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Red ball button
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier
                .width(256.dp) // 3 * 80dp (balls) + 2 * 8dp (spaces)
                .height(56.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = SnookerBall.Red.color)
        ) {
            Text(
                text = "Czerwona",
                color = SnookerBall.Red.contentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Other colors in a FlowRow
        FlowRow(
            modifier = Modifier.width(256.dp), // To align with the button above
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3
        ) {
            colors.forEach { ball ->
                BallButton(ball = ball, onClick = { /*TODO*/ })
            }
        }
    }
}

@Composable
private fun BallButton(ball: SnookerBall, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ball.color),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = ball.points.toString(),
            color = ball.contentColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActionButtons() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Faul") }
            Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Odstawna") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Pudło") }
            Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Cofnij") }
        }
    }
}

@Composable
private fun FrameAndMatchActions() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
            Text("Zakończ frejma")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Powtórz frejma") }
            OutlinedButton(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) { Text("Zakończ mecz") }
        }
    }
}

package com.example.snookerstats.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.snookerstats.domain.model.SnookerBall
import com.example.snookerstats.ui.navigation.BottomNavItem
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ScoringScreen(
    navController: NavController,
    viewModel: ScoringViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navEvent.collectLatest { event ->
            when(event) {
                is ScoringNavEvent.NavigateToMatchHistory -> {
                    navController.navigate(BottomNavItem.MatchHistory.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.showFoulDialog) {
        FoulDialog(
            onDismiss = viewModel::onDismissFoulDialog,
            onConfirm = viewModel::onFoulConfirmed
        )
    }

    if (state.showRepeatFrameDialog) {
        RepeatFrameDialog(
            onDismiss = viewModel::onDismissRepeatFrameDialog,
            onConfirm = viewModel::onRepeatFrameConfirmed
        )
    }

    if (state.showEndMatchDialog) {
        EndMatchDialog(
            onDismiss = viewModel::onDismissEndMatchDialog,
            onConfirm = viewModel::onEndMatchConfirmed
        )
    }

    if (state.showFrameOverDialog) {
        FrameOverDialog(
            winnerName = state.frameWinnerName,
            score = state.frameEndScore,
            onNextFrame = viewModel::onNextFrameClicked,
            onEndMatch = viewModel::onEndMatchConfirmed,
            onReturnToFrame = viewModel::onReturnToFrameClicked
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Scoreboard(
            player1 = state.player1,
            player2 = state.player2,
            activePlayerId = state.activePlayerId
        )
        Spacer(modifier = Modifier.height(16.dp))
        CurrentStatsAndTimer(
            time = state.timer,
            breakValue = state.currentBreak,
            pointsRemaining = state.pointsRemaining,
            redsRemaining = state.redsRemaining
        )
        Spacer(modifier = Modifier.height(8.dp))
        BreakVisualizer(breakBalls = state.breakHistory)
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        BallButtons(
            isFrameOver = state.isFrameOver,
            canPotColor = state.canPotColor,
            isFreeBall = state.isFreeBall,
            redsRemaining = state.redsRemaining,
            nextColorBallOn = state.nextColorBallOn,
            onBallClick = viewModel::onBallClicked
        )
        Spacer(modifier = Modifier.height(16.dp))
        ActionButtons(
            onFoulClick = viewModel::onFoulClicked,
            onSafetyClick = viewModel::onSafetyClicked,
            onMissClick = viewModel::onMissClicked,
            onUndoClick = viewModel::onUndoClicked
        )
        Spacer(modifier = Modifier.weight(1f))
        
        FrameAndMatchActions(
            isBreakInProgress = state.currentBreak > 0,
            onEndFrameClick = viewModel::onEndFrameClicked,
            onRepeatFrameClick = viewModel::onRepeatFrameClicked,
            onEndMatchClick = viewModel::onEndMatchClicked
        )
    }
}

@Composable
private fun FrameOverDialog(
    winnerName: String?,
    score: String?,
    onNextFrame: () -> Unit,
    onEndMatch: () -> Unit,
    onReturnToFrame: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onReturnToFrame,
        title = { Text("Frejm zakończony") },
        text = {
            Column {
                Text("Wygrał: ${winnerName ?: "..."}")
                Text("Wynikiem: ${score ?: "..."}")
            }
        },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End) {
                Button(onClick = onNextFrame) { Text("Następny frejm") }
                TextButton(onClick = onEndMatch) { Text("Zakończ mecz") }
            }
        },
        dismissButton = {
            TextButton(onClick = onReturnToFrame) { Text("Wróć do frejma") }
        }
    )
}


@Composable
private fun EndMatchDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zakończyć mecz?") },
        text = { Text("Czy na pewno chcesz zakończyć ten mecz? Tej akcji nie można cofnąć.") },
        confirmButton = { Button(onClick = onConfirm) { Text("Zakończ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
private fun RepeatFrameDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Powtórzyć frejma?") },
        text = { Text("Ta akcja jest nieodwracalna. Obecny postęp w tym frejmie zostanie usunięty.") },
        confirmButton = { Button(onClick = onConfirm) { Text("Potwierdź") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
private fun FoulDialog(onDismiss: () -> Unit, onConfirm: (Int, Boolean, Int) -> Unit) {
    var selectedPoints by remember { mutableStateOf(4) }
    var isFreeBall by remember { mutableStateOf(false) }
    var redsPotted by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zgłoś faul") },
        text = {
            Column {
                Text("Wybierz wartość faulu:")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    (4..7).forEach { points ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedPoints == points, onClick = { selectedPoints = points })
                            Text(text = points.toString())
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth().toggleable(value = isFreeBall, onValueChange = { isFreeBall = it }, role = Role.Checkbox).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFreeBall, onCheckedChange = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wolna bila (Free ball)")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Czerwone wbite w faulu:")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { redsPotted = (redsPotted - 1).coerceAtLeast(0) }) { Icon(Icons.Default.Remove, "Odejmij") }
                    Text(text = redsPotted.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    IconButton(onClick = { redsPotted = (redsPotted + 1).coerceAtMost(15) }) { Icon(Icons.Default.Add, "Dodaj") }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(selectedPoints, isFreeBall, redsPotted) }) { Text("Zatwierdź") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
private fun Scoreboard(
    player1: PlayerState?,
    player2: PlayerState?,
    activePlayerId: String?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val player1Modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (activePlayerId == player1?.user?.uid) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .padding(vertical = 8.dp, horizontal = 4.dp)

            PlayerInfo(player = player1, modifier = player1Modifier)

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(text = "FRAMES", style = MaterialTheme.typography.labelSmall)
                Text(text = "${player1?.framesWon ?: 0} - ${player2?.framesWon ?: 0}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            val player2Modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (activePlayerId == player2?.user?.uid) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .padding(vertical = 8.dp, horizontal = 4.dp)

            PlayerInfo(player = player2, modifier = player2Modifier)
        }
    }
}

@Composable
private fun PlayerInfo(player: PlayerState?, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = "${player?.user?.firstName ?: "Gracz"} ${player?.user?.lastName ?: ""}",
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )
        Text(
            text = "@${player?.user?.username ?: "..."}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = (player?.score ?: 0).toString(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CurrentStatsAndTimer(time: String, breakValue: Int, pointsRemaining: Int, redsRemaining: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Break", fontSize = 12.sp); Text(breakValue.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Pozostało", fontSize = 12.sp); Text(pointsRemaining.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Czerwonych", fontSize = 12.sp); Text(redsRemaining.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Czas podejścia", fontSize = 12.sp); Text(time, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BreakVisualizer(breakBalls: List<SnookerBall>) {
    val groupedBalls = breakBalls.groupBy { it }.mapValues { it.value.size }.entries.sortedBy { it.key.points }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally), modifier = Modifier.fillMaxWidth().height(24.dp).padding(horizontal = 16.dp)) {
        if (groupedBalls.isEmpty()) Text("Brak bil w brejku", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        else groupedBalls.forEach { (ball, count) -> BallIcon(ball = ball, count = count) }
    }
}

@Composable
private fun BallIcon(ball: SnookerBall, count: Int) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) { drawCircle(color = ball.color) }
        if (count > 0) Text(text = count.toString(), color = ball.contentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BallButtons(isFrameOver: Boolean, canPotColor: Boolean, isFreeBall: Boolean, redsRemaining: Int, nextColorBallOn: SnookerBall?, onBallClick: (SnookerBall) -> Unit) {
    val colors = listOf(SnookerBall.Yellow, SnookerBall.Green, SnookerBall.Brown, SnookerBall.Blue, SnookerBall.Pink, SnookerBall.Black)
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { onBallClick(SnookerBall.Red) },
            enabled = !isFrameOver && redsRemaining > 0 && !isFreeBall,
            modifier = Modifier.width(256.dp).height(56.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = SnookerBall.Red.color, contentColor = SnookerBall.Red.contentColor)
        ) {
            Text("Czerwona", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        FlowRow(modifier = Modifier.width(256.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), maxItemsInEachRow = 3) {
            colors.forEach { ball ->
                val isEnabled = !isFrameOver && when {
                    isFreeBall -> true
                    redsRemaining == 0 -> nextColorBallOn == null || ball == nextColorBallOn
                    else -> canPotColor
                }
                BallButton(ball = ball, onClick = { onBallClick(ball) }, enabled = isEnabled)
            }
        }
    }
}

@Composable
private fun BallButton(ball: SnookerBall, onClick: () -> Unit, enabled: Boolean) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        modifier = Modifier.size(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ball.color, contentColor = ball.contentColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = ball.points.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActionButtons(onFoulClick: () -> Unit, onSafetyClick: () -> Unit, onMissClick: () -> Unit, onUndoClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onSafetyClick, modifier = Modifier.weight(1f)) { Text("Odstawna") }
            Button(onClick = onMissClick, modifier = Modifier.weight(1f)) { Text("Pudło") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onFoulClick, modifier = Modifier.weight(1f)) { Text("Faul") }
            Button(onClick = onUndoClick, modifier = Modifier.weight(1f)) { Text("Cofnij") }
        }
    }
}

@Composable
private fun FrameAndMatchActions(
    isBreakInProgress: Boolean,
    onEndFrameClick: () -> Unit,
    onRepeatFrameClick: () -> Unit,
    onEndMatchClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onEndFrameClick, modifier = Modifier.fillMaxWidth(), enabled = !isBreakInProgress) { Text("Zakończ frejma") }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onRepeatFrameClick, modifier = Modifier.weight(1f), enabled = !isBreakInProgress) { Text("Powtórz frejma") }
            OutlinedButton(onClick = onEndMatchClick, modifier = Modifier.weight(1f), enabled = !isBreakInProgress) { Text("Zakończ mecz") }
        }
    }
}

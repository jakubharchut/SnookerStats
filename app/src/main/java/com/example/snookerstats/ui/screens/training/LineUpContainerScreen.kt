package com.example.snookerstats.ui.screens.training

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineUpContainerScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Trening", "Statystyki")
    var showDescriptionDialog by remember { mutableStateOf(false) }

    if (showDescriptionDialog) {
        AlertDialog(
            onDismissRequest = { showDescriptionDialog = false },
            title = { Text("Opis Ćwiczenia: Czyszczenie Linii", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { 
                Column {
                    Text("Ćwiczenie 'Czyszczenie Linii' polega na zbudowaniu maksymalnego breaka, wbijając wszystkie 15 czerwonych bil, po każdej z nich wbijając bilę kolorową.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Po wbiciu ostatniej czerwonej i ostatniego koloru, Twoim zadaniem jest wbicie wszystkich kolorowych bil w prawidłowej sekwencji: Żółta -> Zielona -> Brązowa -> Niebieska -> Różowa -> Czarna.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Celem jest osiągnięcie jak najwyższego wyniku. Pomylenie się lub wciśnięcie przycisku \"Pudło\" skutkuje zakończeniem podejścia i zapisaniem wyniku.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Powodzenia!", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDescriptionDialog = false }) {
                    Text("Zrozumiałem")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trening - Czyszczenie Linii") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("play?initialTabIndex=2") { popUpTo("play?opponentId={opponentId}&initialTabIndex={initialTabIndex}") { inclusive = true } } }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showDescriptionDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Opis ćwiczenia")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> LineUpTrainingScreen(navController = navController)
                1 -> LineUpStatsScreen()
            }
        }
    }
}

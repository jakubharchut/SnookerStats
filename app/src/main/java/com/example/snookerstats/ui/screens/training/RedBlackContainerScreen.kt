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
fun RedBlackContainerScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Trening", "Statystyki")
    var showDescriptionDialog by remember { mutableStateOf(false) }

    if (showDescriptionDialog) {
        AlertDialog(
            onDismissRequest = { showDescriptionDialog = false },
            title = { Text("Opis Ćwiczenia: Czerwona-Czarna", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = { 
                Column {
                    Text("Ćwiczenie polega na budowaniu breaka poprzez naprzemienne wbijanie czerwonej i czarnej bili.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Technika ćwiczenia:", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text("1. Wbij czerwoną bilę.\n2. Wbij czarną bilę.\n3. Wyjmij obie bile i ustaw je ponownie.\n4. Powtarzaj sekwencję.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Celem jest utrzymanie passy jak najdłużej. Pomyłka lub wciśnięcie przycisku 'Pudło' kończy próbę i zapisuje wynik.", style = MaterialTheme.typography.bodyLarge)
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
                title = { Text("Trening - Czerwona-Czarna") },
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
                0 -> RedBlackTrainingScreen()
                1 -> RedBlackStatsScreen()
            }
        }
    }
}

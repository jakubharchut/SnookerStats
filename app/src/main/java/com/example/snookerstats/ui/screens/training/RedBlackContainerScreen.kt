package com.example.snookerstats.ui.screens.training

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedBlackContainerScreen(navController: NavController) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val tabs = listOf("Trening", "Statystyki")

    if (showInfoDialog) {
        TrainingInfoDialog(
            title = "Zasady: Czerwona-Czarna",
            onDismiss = { showInfoDialog = false }
        ) {
            Text(
                "1. Celem ćwiczenia jest naprzemienne wbijanie bil czerwonych i czarnej.\n\n" +
                "2. Po każdej wbitej czerwonej bili, musisz wbić bilę czarną.\n\n" +
                "3. Passa (streak) jest kontynuowana, dopóki nie popełnisz błędu (pudło lub faul).\n\n" +
                "4. Po błędzie passa jest zerowana i zapisywana w statystykach.\n\n" +
                "Ćwiczenie doskonali umiejętność budowania brejka i pozycjonowania do bili czarnej."
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trening - Czerwona-Czarna") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Zasady ćwiczenia")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
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

@Composable
fun TrainingInfoDialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = content,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrozumiałem")
            }
        }
    )
}
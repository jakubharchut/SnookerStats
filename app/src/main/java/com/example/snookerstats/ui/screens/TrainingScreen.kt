package com.example.snookerstats.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

data class TrainingItem(val name: String, val route: String)

@Composable
fun TrainingScreen(navController: NavController) {
    val trainingOptions = listOf(
        TrainingItem("Line-up", "training/line-up"),
        TrainingItem("Wbijanie długich bil", "training/long-potting"),
        TrainingItem("Trening odstawnych", "training/safety-practice")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(trainingOptions) { training ->
                ListItem(
                    headlineContent = { Text(training.name) },
                    modifier = Modifier.clickable { /* TODO: navController.navigate(training.route) */ }
                )
                Divider()
            }
        }
    }
}

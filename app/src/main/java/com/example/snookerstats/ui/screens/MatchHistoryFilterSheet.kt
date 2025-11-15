package com.example.snookerstats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchHistoryFilterSheet(
    onDismiss: () -> Unit,
    onApplyFilters: (HistoryFilters) -> Unit,
    initialFilters: HistoryFilters
) {
    val sheetState = rememberModalBottomSheetState()
    var filters by remember { mutableStateOf(initialFilters) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Filtry", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

            // Time Filter
            Text("Okres", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeFilter.values().forEach { filter ->
                    FilterChip(
                        selected = filters.timeFilter == filter,
                        onClick = { filters = filters.copy(timeFilter = filter) },
                        label = { Text(filter.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Match Type Filter
            Text("Rodzaj meczu", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filters.matchType == null,
                    onClick = { filters = filters.copy(matchType = null) },
                    label = { Text("Wszystkie") }
                )
                com.example.snookerstats.domain.model.MatchType.values().forEach { filter ->
                    FilterChip(
                        selected = filters.matchType == filter,
                        onClick = { filters = filters.copy(matchType = filter) },
                        label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = { onApplyFilters(filters) }) {
                Text("Zastosuj")
            }
        }
    }
}
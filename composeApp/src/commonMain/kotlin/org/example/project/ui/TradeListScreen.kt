package org.example.project.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.domain.model.Trade
import org.example.project.domain.repository.TradeRepository

@Composable
fun TradeListScreen(
    repository: TradeRepository,
    onAddClick: () -> Unit
) {

    var trades by remember { mutableStateOf(emptyList<Trade>()) }

    // Load trades from database when screen opens
    LaunchedEffect(Unit) {
        repository.getAllTrades().collect { list ->
            trades = list
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) { // ✅ Now opens the Add Screen
                Text("+")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = "Your Trades",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn {
                items(trades) { trade ->
                    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "${trade.direction} ${trade.instrument}", style = MaterialTheme.typography.titleMedium)
                            // Display PnL if it exists, otherwise show "Open"
                            val pnlText = trade.pnl?.toString() ?: "Open Position"
                            Text(text = "PnL: $pnlText", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
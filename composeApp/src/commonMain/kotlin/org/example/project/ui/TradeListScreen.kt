package org.example.project.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.domain.model.Trade
import org.example.project.domain.model.TradeDirection
import org.example.project.domain.model.TradeStatus
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

    val openTrades = trades.count { it.status == TradeStatus.OPEN }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Trade Journal") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ElevatedCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text(
                                text = trades.size.toString(),
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "Total trades",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Column {
                            Text(
                                text = openTrades.toString(),
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "Open positions",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            if (trades.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "No trades yet",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Add your first trade to see insights and analytics.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    FilledTonalButton(onClick = onAddClick) {
                        Text("Add a trade")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trades) { trade ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = trade.instrument,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = trade.marketType.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    AssistChip(
                                        onClick = {},
                                        label = {
                                            Text(
                                                text = if (trade.direction == TradeDirection.BUY) "BUY" else "SELL"
                                            )
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                val pnlText = trade.pnl?.toString() ?: "Open Position"
                                Text(
                                    text = "PnL: $pnlText",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "Entry: ${trade.entryPrice}",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    trade.exitPrice?.let { exit ->
                                        Text(
                                            text = "Exit: $exit",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

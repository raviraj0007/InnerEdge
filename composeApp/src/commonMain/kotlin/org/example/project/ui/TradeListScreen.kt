package org.example.project.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.domain.model.*
import org.example.project.domain.repository.TradeRepository
import kotlin.random.Random

@Composable
fun TradeListScreen(repository: TradeRepository) {
    val scope = rememberCoroutineScope()
    var trades by remember { mutableStateOf(emptyList<Trade>()) }

    // Load trades from database when screen opens
    LaunchedEffect(Unit) {
        repository.getAllTrades().collect { list ->
            trades = list
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scope.launch {
                    // Create a dummy trade to test database insertion
                    val newTrade = Trade(
                        id = Random.nextLong().toString(),
                        date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                        instrument = "NIFTY 50",
                        marketType = MarketType.FNO,
                        direction = TradeDirection.BUY,
                        entryPrice = 100.0,
                        exitPrice = 110.0,
                        quantity = 50,
                        stopLoss = 90.0,
                        target = 120.0,
                        riskPercent = 1.0,
                        pnl = 500.0,
                        status = TradeStatus.CLOSED
                    )
                    repository.insertTrade(newTrade)
                }
            }) {
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
                            Text(text = "PnL: ${trade.pnl}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
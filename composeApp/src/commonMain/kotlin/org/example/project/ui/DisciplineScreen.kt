package org.example.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.domain.model.Trade
import org.example.project.domain.model.emotionStats
import org.example.project.domain.repository.TradeRepository

@Composable
fun DisciplineScreen(repository: TradeRepository) {
    var trades by remember { mutableStateOf(emptyList<Trade>()) }

    LaunchedEffect(Unit) {
        repository.getAllTrades().collect { trades = it }
    }

    val emotionalTrades = remember(trades) { trades.filter { it.emotion.isNotBlank() || it.mistakes.isNotEmpty() } }
    val stats = remember(trades) { trades.emotionStats() }
    val maxMagnitude = stats.maxOfOrNull { kotlin.math.abs(it.totalPnl) }?.takeIf { it > 0 } ?: 1.0

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Discipline", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        items(stats, key = { it.emotion }) { stat ->
            EmotionSummaryCard(
                emotion = stat.emotion,
                tradeCount = stat.tradeCount,
                totalPnl = stat.totalPnl,
                averageWin = stat.averageWin,
                averageLoss = stat.averageLoss,
                normalized = kotlin.math.abs(stat.totalPnl) / maxMagnitude
            )
        }

        item {
            Text("Psychology Entries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        items(emotionalTrades, key = { it.id }) { trade ->
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${trade.date} • ${trade.instrument}", fontWeight = FontWeight.SemiBold)
                    Text("Emotion: ${trade.emotion}")
                    if (trade.mistakes.isNotEmpty()) {
                        Text("Notes: ${trade.mistakes.joinToString()}")
                    }
                    Button(onClick = { }) {
                        Text("Edit psychology notes")
                    }
                }
            }
        }
    }
}

@Composable
fun EmotionSummaryCard(
    emotion: String,
    tradeCount: Int,
    totalPnl: Double,
    averageWin: Double,
    averageLoss: Double,
    normalized: Double
) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(emotion, style = MaterialTheme.typography.titleMedium)
            Text("Trades: $tradeCount")
            Text("Total P&L: ${formatCurrency(totalPnl)}", color = pnlColor(totalPnl))
            Text("Avg Win: ${formatCurrency(averageWin)}")
            Text("Avg Loss: ${formatCurrency(averageLoss)}")
            Row(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth(normalized.toFloat())
                        .height(8.dp)
                        .background(
                            color = if (totalPnl >= 0) Color(0xFF66BB6A) else Color(0xFFEF5350),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }
        }
    }
}

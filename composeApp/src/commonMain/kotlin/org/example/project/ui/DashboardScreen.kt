package org.example.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.example.project.domain.model.Trade
import org.example.project.domain.model.groupTradesByDate
import org.example.project.domain.model.TradeStatus
import org.example.project.domain.repository.TradeRepository

@Composable
fun DashboardScreen(repository: TradeRepository) {
    var trades by remember { mutableStateOf(emptyList<Trade>()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(Unit) {
        repository.getAllTrades().collect { trades = it }
    }

    val selectedMonthTrades = remember(trades) {
        trades.groupBy { it.date.monthNumber }.maxByOrNull { it.value.size }?.value ?: trades
    }
    val byDate = remember(selectedMonthTrades) { selectedMonthTrades.groupTradesByDate() }
    val totalPnl = selectedMonthTrades.sumOf { it.pnl ?: 0.0 }
    val closed = selectedMonthTrades.filter { it.status == TradeStatus.CLOSED }
    val wins = closed.count { (it.pnl ?: 0.0) > 0.0 }
    val winRate = if (closed.isEmpty()) 0.0 else (wins.toDouble() / closed.size) * 100.0
    val bestDay = byDate.maxByOrNull { (_, dayTrades) -> dayTrades.sumOf { it.pnl ?: 0.0 } }
    val worstDay = byDate.minByOrNull { (_, dayTrades) -> dayTrades.sumOf { it.pnl ?: 0.0 } }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Dashboard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            CalendarHeatmap(
                tradesByDate = byDate,
                onDateClick = { selectedDate = it }
            )
        }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard("Total P&L", formatCurrency(totalPnl), pnlColor(totalPnl))
                SummaryCard("Win Rate", "${winRate.toInt()}%")
                SummaryCard(
                    "Best Day",
                    bestDay?.let { "${it.key}: ${formatCurrency(it.value.sumOf { t -> t.pnl ?: 0.0 })}" } ?: "N/A"
                )
                SummaryCard(
                    "Worst Day",
                    worstDay?.let { "${it.key}: ${formatCurrency(it.value.sumOf { t -> t.pnl ?: 0.0 })}" } ?: "N/A"
                )
            }
        }
    }

    selectedDate?.let { date ->
        DayDetailModal(
            date = date,
            dayTrades = byDate[date].orEmpty(),
            onDismiss = { selectedDate = null }
        )
    }
}

@Composable
fun CalendarHeatmap(
    tradesByDate: Map<LocalDate, List<Trade>>,
    onDateClick: (LocalDate) -> Unit
) {
    val days = tradesByDate.keys.sorted()
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Monthly Heatmap", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                days.forEach { day ->
                    val pnl = tradesByDate[day].orEmpty().sumOf { it.pnl ?: 0.0 }
                    val color = when {
                        tradesByDate[day].isNullOrEmpty() -> Color(0xFFBDBDBD)
                        pnl > 0 -> Color(0xFF4CAF50)
                        pnl < 0 -> Color(0xFFE53935)
                        else -> Color(0xFFBDBDBD)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color = color, shape = RoundedCornerShape(8.dp))
                            .clickable { onDateClick(day) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(day.dayOfMonth.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun DayDetailModal(
    date: LocalDate,
    dayTrades: List<Trade>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("$date trade summary") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Trades: ${dayTrades.size}")
                Text("P&L: ${formatCurrency(dayTrades.sumOf { it.pnl ?: 0.0 })}")
                dayTrades.take(5).forEach {
                    Text("• ${it.instrument} ${it.strategy.ifBlank { "No strategy" }} ${formatCurrency(it.pnl ?: 0.0)}")
                }
            }
        }
    )
}

@Composable
private fun SummaryCard(title: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

internal fun formatCurrency(value: Double): String = if (value >= 0) "+₹${"%.2f".format(value)}" else "-₹${"%.2f".format(-value)}"

@Composable
internal fun pnlColor(value: Double): Color = when {
    value > 0 -> Color(0xFF2E7D32)
    value < 0 -> Color(0xFFC62828)
    else -> MaterialTheme.colorScheme.onSurface
}

package org.example.project.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.domain.model.Trade
import org.example.project.domain.model.TradeDirection
import org.example.project.domain.model.TradeStatus
import org.example.project.domain.repository.TradeRepository
import org.example.project.ui.util.formatCurrency
import org.example.project.ui.util.pnlColor

private enum class TradeFilter(val label: String) { ALL("All"), OPEN("Open"), CLOSED("Closed"), WIN("Win"), LOSS("Loss") }

@Composable
fun TradeListScreen(repository: TradeRepository, onAddClick: () -> Unit) {
    var trades by remember { mutableStateOf(emptyList<Trade>()) }
    var selectedFilter by remember { mutableStateOf(TradeFilter.ALL) }
    var fabExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { repository.getAllTrades().collect { trades = it } }

    val filteredTrades = remember(trades, selectedFilter) {
        trades.filter { trade ->
            when (selectedFilter) {
                TradeFilter.ALL -> true
                TradeFilter.OPEN -> trade.status == TradeStatus.OPEN
                TradeFilter.CLOSED -> trade.status == TradeStatus.CLOSED
                TradeFilter.WIN -> (trade.pnl ?: 0.0) > 0.0
                TradeFilter.LOSS -> (trade.pnl ?: 0.0) < 0.0
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            ExpandableFabMenu(
                expanded = fabExpanded,
                onMainFabClick = { fabExpanded = !fabExpanded },
                onAddLiveTrade = { fabExpanded = false; onAddClick() },
                onAddBacktestTrade = { fabExpanded = false; onAddClick() },
                onAddPsychologyEntry = { fabExpanded = false; onAddClick() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Text("Trades", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            item { FilterRow(selectedFilter = selectedFilter, onFilterSelected = { selectedFilter = it }) }

            if (filteredTrades.isEmpty()) {
                item { EmptyStateCard(onAddClick = onAddClick) }
            } else {
                items(filteredTrades, key = { it.id }) { trade -> TradeStoryCard(trade = trade) }
            }
        }
    }
}

@Composable
private fun FilterRow(selectedFilter: TradeFilter, onFilterSelected: (TradeFilter) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(TradeFilter.entries) { filter ->
            FilterChip(selected = filter == selectedFilter, onClick = { onFilterSelected(filter) }, label = { Text(filter.label) })
        }
    }
}

@Composable
private fun TradeStoryCard(trade: Trade) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(trade.instrument, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                AssistChip(onClick = {}, label = { Text(if (trade.direction == TradeDirection.BUY) "BUY" else "SELL") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PriceInfo("Entry", trade.entryPrice)
                PriceInfo("SL", trade.stopLoss)
                PriceInfo("TP", trade.target)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("P&L", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = trade.pnl?.let { formatCurrency(it) } ?: "Open Position",
                        style = MaterialTheme.typography.titleMedium,
                        color = pnlColor(trade.pnl ?: 0.0),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.End) {
                    Text("Risk", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(trade.riskPercent?.let { "${"%.2f".format(it)}%" } ?: "—", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Text("Strategy: ${trade.strategy?.ifBlank { "Not tagged" }}", style = MaterialTheme.typography.bodyMedium)
            Text("Emotion: ${trade.emotion}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            if (trade.mistakes.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(trade.mistakes) { mistake ->
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)) {
                            Text(mistake, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceInfo(text: String, value: Double?) {
    Text(text = "$text: ${value?.let { "%.2f".format(it) } ?: "—"}", style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun EmptyStateCard(onAddClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("No trades for this filter", style = MaterialTheme.typography.titleMedium)
            Text("Add a trade to start building your complete trading story.", style = MaterialTheme.typography.bodyMedium)
            ExtendedFloatingActionButton(
                onClick = onAddClick
            ) {
                Text("Add Trade")
            }

        }
    }
}

@Composable
private fun ExpandableFabMenu(
    expanded: Boolean,
    onMainFabClick: () -> Unit,
    onAddLiveTrade: () -> Unit,
    onAddBacktestTrade: () -> Unit,
    onAddPsychologyEntry: () -> Unit
) {
    val rotation by animateFloatAsState(targetValue = if (expanded) 45f else 0f)
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AnimatedVisibility(visible = expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniFabAction("📈 Add Live Trade", onAddLiveTrade)
                MiniFabAction("🧪 Add Backtest Trade", onAddBacktestTrade)
                MiniFabAction("🧠 Add Psychology Entry", onAddPsychologyEntry)
            }
        }
        FloatingActionButton(onClick = onMainFabClick, shape = CircleShape) {
            Text(text = "+", modifier = Modifier.rotate(rotation), style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun MiniFabAction(label: String, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
            defaultElevation = 3.dp
        )
    ) {
        Text(label)
    }
}

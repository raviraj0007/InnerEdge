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
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.example.project.domain.model.Trade
import org.example.project.domain.model.TradeDirection
import org.example.project.domain.model.TradeStatus
import org.example.project.domain.repository.TradeRepository
import kotlin.math.abs

private enum class TradeFilter(val label: String) {
    ALL("All"),
    OPEN("Open"),
    CLOSED("Closed"),
    WIN("Win"),
    LOSS("Loss")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeListScreen(
    repository: TradeRepository,
    onAddClick: () -> Unit
) {
    var trades by remember { mutableStateOf(emptyList<Trade>()) }
    var selectedFilter by remember { mutableStateOf(TradeFilter.ALL) }
    var isFabExpanded by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repository.getAllTrades().collect { tradeList ->
            trades = tradeList
        }
    }

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
        topBar = { CenterAlignedTopAppBar(title = { Text("Trade Journal") }) },
        containerColor = Color.White,
        floatingActionButton = {
            ExpandableFabMenu(
                expanded = fabExpanded,
                onMainFabClick = {
                    fabExpanded = !fabExpanded
                    if (!fabExpanded) onAddClick()
                },
                onAddLiveTrade = {
                    fabExpanded = false
                    onAddClick()
                },
                onAddBacktestTrade = {
                    fabExpanded = false
                    onAddClick()
                },
                onAddPsychologyEntry = {
                    fabExpanded = false
                    onAddClick()
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OverviewCard(trades = trades)
            }

            item {
                FilterRow(selectedFilter = selectedFilter, onFilterSelected = { selectedFilter = it })
            }

            if (filteredTrades.isEmpty()) {
                item {
                    EmptyStateCard(onAddClick = onAddClick)
                }
            } else {
                items(filteredTrades, key = { it.id }) { trade ->
                    TradeStoryCard(trade = trade)
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(trades: List<Trade>) {
    val closedTrades = trades.filter { it.status == TradeStatus.CLOSED && it.pnl != null }
    val winningTrades = closedTrades.count { (it.pnl ?: 0.0) > 0.0 }
    val winRate = if (closedTrades.isNotEmpty()) {
        (winningTrades.toDouble() / closedTrades.size.toDouble()) * 100
    } else {
        0.0
    }

    val totalPnl = trades.sumOf { it.pnl ?: 0.0 }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val todayPnl = trades.filter { it.date == today }.sumOf { it.pnl ?: 0.0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFEAF2FF), Color(0xFFF2EEFF))
                    )
                )
                .padding(16.dp)
        ) {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricColumn(
                    modifier = Modifier.weight(1f),
                    value = trades.size.toString(),
                    label = "Total Trades"
                )
                VerticalDivider()
                MetricColumn(
                    modifier = Modifier.weight(1f),
                    value = "${winRate.format(0)}%",
                    label = "Win Rate",
                    valueColor = if (winRate >= 50) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                )
                VerticalDivider()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "Total P&L",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(totalPnl),
                        style = MaterialTheme.typography.titleMedium,
                        color = pnlColor(totalPnl),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Today: ${formatCurrency(todayPnl)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = pnlColor(todayPnl)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(
    modifier: Modifier,
    value: String,
    label: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = modifier.padding(horizontal = 12.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(52.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    )
}

@Composable
private fun FilterRow(
    selectedFilter: TradeFilter,
    onFilterSelected: (TradeFilter) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(TradeFilter.entries) { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) }
            )
        }
    }
}

@Composable
private fun TradeStoryCard(trade: Trade) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trade.instrument,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                AssistChip(
                    onClick = {},
                    label = { Text(if (trade.direction == TradeDirection.BUY) "BUY" else "SELL") }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PriceInfo(text = "Entry", value = trade.entryPrice)
                PriceInfo(text = "SL", value = trade.stopLoss)
                PriceInfo(text = "TP", value = trade.target)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "P&L",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trade.pnl?.let { formatCurrency(it) } ?: "Open Position",
                        style = MaterialTheme.typography.titleMedium,
                        color = pnlColor(trade.pnl ?: 0.0),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Risk",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trade.riskPercent?.let { "${it.format(2)}%" } ?: "—",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Text(
                text = "Strategy: ${trade.strategy ?: "Not tagged"}",
                style = MaterialTheme.typography.bodyMedium
            )

            trade.emotion?.let {
                Text(
                    text = "Emotion: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (trade.mistakes.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(trade.mistakes) { mistake ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = mistake,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceInfo(text: String, value: Double?) {
    Text(
        text = "$text: ${value?.format(2) ?: "—"}",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun EmptyStateCard(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("No trades for this filter", style = MaterialTheme.typography.titleMedium)
            Text(
                "Add a trade to start building your complete trading story.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ExtendedFloatingActionButton(onClick = onAddClick, text = { Text("Add Trade") })
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

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniFabAction(label = "📈 Add Live Trade", onClick = onAddLiveTrade)
                MiniFabAction(label = "🧪 Add Backtest Trade", onClick = onAddBacktestTrade)
                MiniFabAction(label = "🧠 Add Psychology Entry", onClick = onAddPsychologyEntry)
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
        text = { Text(label) },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    )
}

private fun formatCurrency(value: Double): String {
    val amount = value.format(2)
    return if (value >= 0) "+₹$amount" else "-₹${kotlin.math.abs(value).format(2)}"
}

@Composable
private fun pnlColor(value: Double): Color = when {
    value > 0 -> Color(0xFF2E7D32)
    value < 0 -> Color(0xFFC62828)
    else -> MaterialTheme.colorScheme.onSurface
}

private fun Double.format(decimals: Int): String = ("%." + decimals + "f").format(this)

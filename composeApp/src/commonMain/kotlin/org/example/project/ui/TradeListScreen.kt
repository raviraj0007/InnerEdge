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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.example.project.domain.model.Trade
import org.example.project.domain.model.TradeDirection
import org.example.project.domain.model.TradeStatus
import org.example.project.domain.repository.TradeRepository
import org.example.project.domain.util.calculateWinRate
import org.example.project.ui.util.UiConstants
import org.example.project.ui.util.format
import org.example.project.ui.util.formatCurrency
import org.example.project.ui.util.pnlColor
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

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
    onAddClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    var trades by remember { mutableStateOf(emptyList<Trade>()) }
    var selectedFilter by remember { mutableStateOf(TradeFilter.ALL) }
    var isFabExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repository.getAllTrades().collect { trades = it }
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
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Trade Journal") }
            )
        },
        floatingActionButton = {
            ExpandableFabMenu(
                expanded = isFabExpanded,
                onMainFabClick = { isFabExpanded = !isFabExpanded },
                onAddLiveTrade = {
                    isFabExpanded = false
                    onAddClick()
                },
                onAddBacktestTrade = {
                    isFabExpanded = false
                    onAddClick()
                },
                onAddPsychologyEntry = {
                    isFabExpanded = false
                    onAddClick()
                }
            )
        }
    ) { innerPadding ->
        val mergedPadding = PaddingValues(
            start = contentPadding.calculateLeftPadding(LayoutDirection.Ltr) + innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
            top = contentPadding.calculateTopPadding() + innerPadding.calculateTopPadding(),
            end = contentPadding.calculateRightPadding(LayoutDirection.Ltr) + innerPadding.calculateRightPadding(LayoutDirection.Ltr),
            bottom = contentPadding.calculateBottomPadding() + innerPadding.calculateBottomPadding()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(mergedPadding),
            contentPadding = PaddingValues(horizontal = UiConstants.MediumSpacing, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)
        ) {
            item { OverviewMetricsCard(trades = trades) }
            item {
                TradeFilterRow(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }

            if (filteredTrades.isEmpty()) {
                item { EmptyStateCard(onAddClick = onAddClick) }
            } else {
                items(filteredTrades, key = { it.id }) { trade ->
                    TradeStoryCard(trade = trade)
                }
            }
        }
    }
}

@Composable
private fun OverviewMetricsCard(trades: List<Trade>) {
    val winRate = calculateWinRate(trades)
    val totalPnl = trades.sumOf { it.pnl ?: 0.0 }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val todayPnl = trades.filter { it.date == today }.sumOf { it.pnl ?: 0.0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(UiConstants.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFEAF2FF), Color(0xFFF1EDFF))
                    )
                )
                .padding(UiConstants.MediumSpacing)
        ) {
            Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricItem(
                    modifier = Modifier.weight(1f),
                    value = trades.size.toString(),
                    label = "Total Trades"
                )
                ThinDivider()
                MetricItem(
                    modifier = Modifier.weight(1f),
                    value = "${winRate.format(0)}%",
                    label = "Win Rate",
                    valueColor = if (winRate >= 50) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                )
                ThinDivider()
                Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text("Total P&L", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text(
                        formatCurrency(totalPnl),
                        style = MaterialTheme.typography.titleMedium,
                        color = pnlColor(totalPnl),
                        fontWeight = FontWeight.Bold
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
private fun MetricItem(modifier: Modifier, value: String, label: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier = modifier.padding(horizontal = 10.dp)) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = valueColor)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

@Composable
private fun ThinDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(52.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    )
}

@Composable
private fun TradeFilterRow(selectedFilter: TradeFilter, onFilterSelected: (TradeFilter) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)) {
        items(TradeFilter.entries) { filter ->
            FilterChip(selected = selectedFilter == filter, onClick = { onFilterSelected(filter) }, label = { Text(filter.label) })
        }
    }
}

@Composable
private fun TradeStoryCard(trade: Trade) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), RoundedCornerShape(UiConstants.CardCornerRadius)),
        shape = RoundedCornerShape(UiConstants.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UiConstants.MediumSpacing),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(trade.instrument, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(trade.marketType.name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                }
                AssistChip(onClick = {}, label = { Text(if (trade.direction == TradeDirection.BUY) "BUY" else "SELL") })
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PriceStat("Entry", trade.entryPrice)
                PriceStat("SL", trade.stopLoss)
                PriceStat("TP", trade.target)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("P&L", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text(
                        text = trade.pnl?.let { formatCurrency(it) } ?: "Open Position",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor(trade.pnl ?: 0.0)
                    )
                }
                Text("Risk: ${trade.riskPercent?.let { "${it.format(2)}%" } ?: "—"}", style = MaterialTheme.typography.bodyMedium)
            }

            Text("Strategy: ${trade.strategy ?: "Not tagged"}", style = MaterialTheme.typography.bodyMedium)
            Text("Emotion: ${trade.emotion.toEmotionLabel()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)

            if (trade.mistakes.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)) {
                    items(trade.mistakes) { mistake ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F5F8)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Text(mistake, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceStat(label: String, value: Double?) {
    Text(text = "$label: ${value?.format(2) ?: "—"}", style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun EmptyStateCard(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(UiConstants.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)) {
            Text("No trades found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Try another filter or add a new trade.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            ExtendedFloatingActionButton(onClick = onAddClick) {
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

    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)) {
        AnimatedVisibility(visible = expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)) {
                FabActionButton("📈 Add Live Trade", onAddLiveTrade)
                FabActionButton("🧪 Add Backtest Trade", onAddBacktestTrade)
                FabActionButton("🧠 Add Psychology Entry", onAddPsychologyEntry)
            }
        }

        FloatingActionButton(onClick = onMainFabClick, shape = CircleShape, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
            Text(text = "+", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.rotate(rotation))
        }
    }
}

@Composable
private fun FabActionButton(label: String, onClick: () -> Unit) {
    ExtendedFloatingActionButton(onClick = onClick) {
        Text(label)
    }
}

private fun String?.toEmotionLabel(): String {
    val value = this?.trim().orEmpty()
    return when {
        value.equals("fear", ignoreCase = true) -> "Fear 😬"
        value.equals("greed", ignoreCase = true) -> "Greed 😈"
        value.equals("neutral", ignoreCase = true) -> "Neutral 😌"
        value.isBlank() -> "Neutral 😌"
        else -> "$value 😌"
    }
}

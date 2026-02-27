package com.inneredge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inneredge.presentation.viewmodel.DashboardViewModel
import com.inneredge.ui.components.UiConstants
import com.inneredge.ui.components.formatCurrency
import com.inneredge.ui.components.formatPercent
import com.inneredge.ui.components.pnlColor

@Composable
fun DashboardScreen(viewModel: DashboardViewModel, contentPadding: PaddingValues = PaddingValues()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val trades = state.trades
    val groupedByDate = trades.groupBy { it.date }

    LazyColumn(
        modifier = Modifier.padding(contentPadding),
        contentPadding = PaddingValues(UiConstants.MediumSpacing),
        verticalArrangement = Arrangement.spacedBy(UiConstants.MediumSpacing)
    ) {
        item { Text("Dashboard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)) {
                SummaryCard(Modifier.weight(1f), "Total P&L", formatCurrency(state.totalPnl), pnlColor(state.totalPnl))
                SummaryCard(
                    Modifier.weight(1f),
                    "Win Rate",
                    formatPercent(state.winRate.toDouble(), 1),
                    if (state.winRate >= 50) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        item { Text("Calendar Heatmap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        items(groupedByDate.toList().sortedByDescending { it.first }.take(14)) { (date, dayTrades) ->
            val dayPnl = dayTrades.sumOf { it.pnl ?: 0.0 }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(date.toString(), style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)) {
                    Box(
                        modifier = Modifier.size(UiConstants.MediumSpacing).background(
                            color = when {
                                dayPnl > 0 -> Color(0xFFB8E6C1)
                                dayPnl < 0 -> Color(0xFFF4C2C2)
                                else -> Color(0xFFE8E8E8)
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                    )
                    Text(formatCurrency(dayPnl), color = pnlColor(dayPnl), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun SummaryCard(modifier: Modifier, title: String, value: String, valueColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(UiConstants.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(UiConstants.MediumSpacing)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Spacer(Modifier.height(UiConstants.SmallSpacing))
            Text(value, style = MaterialTheme.typography.titleLarge, color = valueColor, fontWeight = FontWeight.Bold)
        }
    }
}

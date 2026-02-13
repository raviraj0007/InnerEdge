package org.example.project.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.example.project.domain.repository.TradeRepository
import org.example.project.ui.util.UiConstants
import org.example.project.ui.util.formatCurrency
import org.example.project.ui.util.pnlColor

@Composable
fun DisciplineScreen(
    repository: TradeRepository,
    contentPadding: PaddingValues = PaddingValues()
) {
    var trades by remember { mutableStateOf(emptyList<Trade>()) }

    LaunchedEffect(Unit) {
        repository.getAllTrades().collect { trades = it }
    }

    val grouped = trades.groupBy { it.emotion?.ifBlank { "Neutral" } ?: "Neutral" }

    LazyColumn(
        modifier = Modifier.padding(contentPadding),
        contentPadding = PaddingValues(UiConstants.MediumSpacing),
        verticalArrangement = Arrangement.spacedBy(UiConstants.SmallSpacing)
    ) {
        item {
            Text("Discipline", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        items(grouped.toList().sortedByDescending { it.second.size }) { (emotion, emotionTrades) ->
            val totalPnl = emotionTrades.sumOf { it.pnl ?: 0.0 }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(UiConstants.CardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(UiConstants.MediumSpacing)) {
                    Text(emotion, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Trades: ${emotionTrades.size}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = formatCurrency(totalPnl),
                            style = MaterialTheme.typography.bodyLarge,
                            color = pnlColor(totalPnl),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

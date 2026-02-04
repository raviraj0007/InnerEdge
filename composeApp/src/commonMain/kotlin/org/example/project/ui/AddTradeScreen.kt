package org.example.project.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.domain.model.*
import org.example.project.domain.repository.TradeRepository
import kotlin.random.Random

@Composable
fun AddTradeScreen(
    repository: TradeRepository,
    onTradeSaved: () -> Unit, // Callback to go back to the list
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Form State
    var instrument by remember { mutableStateOf("") }
    var entryPrice by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(TradeDirection.BUY) }

    val canSave = instrument.isNotBlank() && entryPrice.toDoubleOrNull() != null

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New Trade") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Trade Details", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = instrument,
                        onValueChange = { instrument = it },
                        label = { Text("Instrument (e.g., NIFTY 50)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = entryPrice,
                        onValueChange = { entryPrice = it },
                        label = { Text("Entry Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Direction", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = { direction = TradeDirection.BUY },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (direction == TradeDirection.BUY) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                contentColor = if (direction == TradeDirection.BUY) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        ) { Text("BUY") }

                        FilledTonalButton(
                            onClick = { direction = TradeDirection.SELL },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (direction == TradeDirection.SELL) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                contentColor = if (direction == TradeDirection.SELL) {
                                    MaterialTheme.colorScheme.onError
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        ) { Text("SELL") }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            val price = entryPrice.toDoubleOrNull() ?: 0.0
                            val newTrade = Trade(
                                id = Random.nextLong().toString(), // Simple ID for now
                                date = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .date,
                                instrument = instrument,
                                marketType = MarketType.FNO, // Defaulting for simplicity
                                direction = direction,
                                entryPrice = price,
                                exitPrice = null,
                                quantity = 50,
                                stopLoss = null,
                                target = null,
                                riskPercent = null,
                                pnl = null,
                                status = TradeStatus.OPEN
                            )
                            repository.insertTrade(newTrade)
                            onTradeSaved() // Navigate back
                        }
                    },
                    enabled = canSave
                ) {
                    Text("Save Trade")
                }
            }
        }
    }
}

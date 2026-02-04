package org.example.project.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    // Form State
    var instrument by remember { mutableStateOf("") }
    var entryPrice by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(TradeDirection.BUY) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Log a New Trade", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Instrument Input
        OutlinedTextField(
            value = instrument,
            onValueChange = { instrument = it },
            label = { Text("Instrument (e.g., NIFTY 50)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Entry Price Input
        OutlinedTextField(
            value = entryPrice,
            onValueChange = { entryPrice = it },
            label = { Text("Entry Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Direction Selection (Simple Row)
        Text("Direction:", style = MaterialTheme.typography.labelLarge)
        Row {
            Button(
                onClick = { direction = TradeDirection.BUY },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (direction == TradeDirection.BUY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) { Text("BUY") }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { direction = TradeDirection.SELL },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (direction == TradeDirection.SELL) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant
                )
            ) { Text("SELL") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                scope.launch {
                    val price = entryPrice.toDoubleOrNull() ?: 0.0
                    val newTrade = Trade(
                        id = Random.nextLong().toString(), // Simple ID for now
                        date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
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
            }) {
                Text("Save Trade")
            }
        }
    }
}
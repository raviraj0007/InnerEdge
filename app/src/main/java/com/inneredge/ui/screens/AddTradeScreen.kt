package com.inneredge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inneredge.domain.model.TradeDirection
import com.inneredge.domain.model.TradeStatus
import com.inneredge.presentation.viewmodel.AddTradeViewModel
import java.time.format.DateTimeFormatter

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
private val predefinedMistakes = listOf(
    "Overtrading",
    "Revenge Trade",
    "FOMO Entry",
    "No Stop Loss",
    "Moved SL",
    "Ignored Setup",
    "Early Exit",
    "Late Entry"
)

@Composable
fun AddTradeScreen(viewModel: AddTradeViewModel, onTradeSaved: () -> Unit, onCancel: () -> Unit) {
    AddEditTradeScreen(viewModel = viewModel, onTradeSaved = onTradeSaved, onCancel = onCancel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTradeScreen(viewModel: AddTradeViewModel, onTradeSaved: () -> Unit, onCancel: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var showCloseDialog by remember { mutableStateOf(false) }
    var closePrice by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (state.isEditing) "Edit Trade" else "New Trade") }
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
                    Text("Basic Information", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.dateTime.format(dateFormatter),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date & Time") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.instrument,
                        onValueChange = viewModel::onInstrumentChange,
                        label = { Text("Instrument") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.strategy,
                        onValueChange = viewModel::onStrategyChange,
                        label = { Text("Strategy Name") },
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
                    Text("Execution", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = { viewModel.onDirectionChange(TradeDirection.BUY) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.direction == TradeDirection.BUY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (state.direction == TradeDirection.BUY) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) { Text("BUY") }

                        FilledTonalButton(
                            onClick = { viewModel.onDirectionChange(TradeDirection.SELL) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.direction == TradeDirection.SELL) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (state.direction == TradeDirection.SELL) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) { Text("SELL") }
                    }

                    OutlinedTextField(
                        value = state.entryPrice,
                        onValueChange = viewModel::onEntryPriceChange,
                        label = { Text("Entry Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.stopLoss,
                        onValueChange = viewModel::onStopLossChange,
                        label = { Text("Stop Loss") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.takeProfit,
                        onValueChange = viewModel::onTakeProfitChange,
                        label = { Text("Take Profit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.quantity,
                        onValueChange = viewModel::onQuantityChange,
                        label = { Text("Lot Size") },
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
                    Text("Risk & Result", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.riskPercent,
                        onValueChange = viewModel::onRiskPercentChange,
                        label = { Text("Risk %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.status == TradeStatus.CLOSED) {
                        OutlinedTextField(
                            value = state.exitPrice,
                            onValueChange = {},
                            label = { Text("Exit Price") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = state.pnlFormatted,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("P&L ₹") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.isEditing && state.status == TradeStatus.OPEN) {
                        Button(onClick = { showCloseDialog = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Close Position")
                        }
                    }
                }
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Psychology", style = MaterialTheme.typography.titleMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            predefinedMistakes.chunked(2).forEach { rowMistakes ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowMistakes.forEach { mistake ->
                                        FilterChip(
                                            selected = mistake in state.mistakes,
                                            onClick = { viewModel.toggleMistake(mistake) },
                                            label = { Text(mistake) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = viewModel::onNotesChange,
                        label = { Text("Notes") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.saveTrade(onTradeSaved) }, enabled = state.canSave) { Text("Save Trade") }
            }
        }
    }

    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text("Close Position") },
            text = {
                OutlinedTextField(
                    value = closePrice,
                    onValueChange = { closePrice = it },
                    label = { Text("Exit Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onExitPriceChange(closePrice)
                        viewModel.closeTrade()
                        showCloseDialog = false
                        closePrice = ""
                    },
                    enabled = closePrice.toDoubleOrNull() != null
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDialog = false }) { Text("Cancel") }
            }
        )
    }
}

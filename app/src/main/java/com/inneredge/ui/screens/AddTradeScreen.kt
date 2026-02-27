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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTradeScreen(viewModel: AddTradeViewModel, onTradeSaved: () -> Unit, onCancel: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var showCloseDialog by remember { mutableStateOf(false) }
    var exitPriceInput by remember { mutableStateOf("") }

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text(if (state.isEditing) "Edit Trade" else "New Trade") }) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Trade Details", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.instrument,
                        onValueChange = viewModel::onInstrumentChange,
                        label = { Text("Instrument (e.g., NIFTY 50)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.entryPrice,
                        onValueChange = viewModel::onEntryPriceChange,
                        label = { Text("Entry Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Direction", style = MaterialTheme.typography.titleMedium)
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
                }
            }

            if (state.isEditing && state.status == TradeStatus.OPEN) {
                Button(onClick = { showCloseDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Close Position")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.saveTrade(onTradeSaved) }, enabled = state.canSave) {
                    Text(if (state.isEditing) "Update Trade" else "Save Trade")
                }
            }
        }
    }

    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text("Close Position") },
            text = {
                OutlinedTextField(
                    value = exitPriceInput,
                    onValueChange = { exitPriceInput = it },
                    label = { Text("Exit Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val exitPrice = exitPriceInput.toDoubleOrNull()
                        if (exitPrice != null) {
                            viewModel.closeTrade(exitPrice, onTradeSaved)
                            showCloseDialog = false
                            exitPriceInput = ""
                        }
                    }
                ) { Text("Close") }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AddTradeScreen(viewModel: AddTradeViewModel, onTradeSaved: () -> Unit, onCancel: () -> Unit) {
    AddEditTradeScreen(viewModel = viewModel, onTradeSaved = onTradeSaved, onCancel = onCancel)
}

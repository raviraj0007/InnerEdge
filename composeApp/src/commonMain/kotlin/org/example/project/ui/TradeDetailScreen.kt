package org.example.project.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.domain.model.TradeStatus
import org.example.project.domain.repository.TradeRepository
import org.example.project.ui.viewmodel.TradeDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeDetailScreen(
    repository: TradeRepository,
    tradeId: String,
    onBack: () -> Unit,
    onEditTrade: (String) -> Unit
) {
    val viewModel = remember(repository) { TradeDetailViewModel(repository) }
    val trade by viewModel.trade.collectAsState()
    val scope = rememberCoroutineScope()

    var showCloseDialog by remember { mutableStateOf(false) }
    var exitPriceInput by remember { mutableStateOf("") }

    LaunchedEffect(tradeId) {
        viewModel.loadTrade(tradeId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Trade Detail") }
            )
        }
    ) { padding ->
        val currentTrade = trade
        if (currentTrade == null) {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Text("Trade not found")
                Spacer(Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Back") }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { DetailRow("Symbol", currentTrade.instrument) }
            item { DetailRow("Entry Price", currentTrade.entryPrice.toString()) }
            item { DetailRow("SL", currentTrade.stopLoss?.toString() ?: "—") }
            item { DetailRow("TP", currentTrade.target?.toString() ?: "—") }
            item { DetailRow("Exit Price", currentTrade.exitPrice?.toString() ?: "—") }
            item { DetailRow("Status", currentTrade.status.name) }
            item { DetailRow("P&L", currentTrade.pnl?.toString() ?: "—") }
            item { DetailRow("Strategy", currentTrade.strategy ?: "—") }
            item { DetailRow("Emotion", currentTrade.emotion ?: "—") }
            item { DetailRow("Notes", currentTrade.notes ?: "—") }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { onEditTrade(currentTrade.id) }, modifier = Modifier.weight(1f)) {
                        Text("Edit Trade")
                    }
                    if (currentTrade.status == TradeStatus.OPEN) {
                        Button(
                            onClick = { showCloseDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close Trade")
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.deleteTrade()
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Trade")
                }
            }
        }

        if (showCloseDialog) {
            AlertDialog(
                onDismissRequest = { showCloseDialog = false },
                title = { Text("Close Trade") },
                text = {
                    OutlinedTextField(
                        value = exitPriceInput,
                        onValueChange = { exitPriceInput = it },
                        label = { Text("Exit Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val exitPrice = exitPriceInput.toDoubleOrNull() ?: return@TextButton
                            scope.launch {
                                viewModel.closeTrade(exitPrice)
                                showCloseDialog = false
                                exitPriceInput = ""
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCloseDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value)
    }
}

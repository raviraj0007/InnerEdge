package org.example.project.ui

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.project.domain.model.TradeDirection
import org.example.project.domain.repository.TradeRepository
import org.example.project.ui.viewmodel.AddTradeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTradeScreen(
    repository: TradeRepository,
    onTradeSaved: () -> Unit,
    onCancel: () -> Unit,
    tradeId: String? = null
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val viewModel = remember(repository) { AddTradeViewModel(repository) }

    val instrument by viewModel.instrument.collectAsState()
    val entryPrice by viewModel.entryPrice.collectAsState()
    val stopLoss by viewModel.stopLoss.collectAsState()
    val target by viewModel.target.collectAsState()
    val strategy by viewModel.strategy.collectAsState()
    val emotion by viewModel.emotion.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val direction by viewModel.direction.collectAsState()

    LaunchedEffect(tradeId) {
        viewModel.loadTrade(tradeId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (tradeId == null) "New Trade" else "Edit Trade") }
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
                        onValueChange = viewModel::onInstrumentChange,
                        label = { Text("Symbol") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = entryPrice,
                        onValueChange = viewModel::onEntryPriceChange,
                        label = { Text("Entry Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = stopLoss,
                        onValueChange = viewModel::onStopLossChange,
                        label = { Text("SL") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = target,
                        onValueChange = viewModel::onTargetChange,
                        label = { Text("TP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = strategy,
                        onValueChange = viewModel::onStrategyChange,
                        label = { Text("Strategy") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emotion,
                        onValueChange = viewModel::onEmotionChange,
                        label = { Text("Emotion") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = viewModel::onNotesChange,
                        label = { Text("Notes") },
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
                            onClick = { viewModel.onDirectionChange(TradeDirection.BUY) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (direction == TradeDirection.BUY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (direction == TradeDirection.BUY) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) { Text("BUY") }

                        FilledTonalButton(
                            onClick = { viewModel.onDirectionChange(TradeDirection.SELL) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (direction == TradeDirection.SELL) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (direction == TradeDirection.SELL) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
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
                            viewModel.saveTrade()
                            onTradeSaved()
                        }
                    },
                    enabled = viewModel.canSave()
                ) {
                    Text(if (tradeId == null) "Save Trade" else "Update Trade")
                }
            }
        }
    }
}

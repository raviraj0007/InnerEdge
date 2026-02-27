package com.inneredge.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inneredge.domain.model.MarketType
import com.inneredge.domain.model.Trade
import com.inneredge.domain.model.TradeDirection
import com.inneredge.domain.model.TradeStatus
import com.inneredge.domain.usecase.GetTradeByIdUseCase
import com.inneredge.domain.usecase.InsertTradeUseCase
import com.inneredge.domain.usecase.UpdateTradeUseCase
import com.inneredge.presentation.state.AddTradeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddTradeViewModel @Inject constructor(
    private val insertTradeUseCase: InsertTradeUseCase,
    private val getTradeByIdUseCase: GetTradeByIdUseCase,
    private val updateTradeUseCase: UpdateTradeUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AddTradeState())
    val state: StateFlow<AddTradeState> = _state.asStateFlow()
    private var editingTradeId: String? = null
    private var originalTrade: Trade? = null
    private val defaultQuantity = "50"

    init {
        _state.update { it.copy(quantity = defaultQuantity) }
    }

    fun onInstrumentChange(value: String) = _state.update { it.copy(instrument = value) }
    fun onEntryPriceChange(value: String) = _state.updateAndRecalculate { it.copy(entryPrice = value) }
    fun onDirectionChange(value: TradeDirection) = _state.updateAndRecalculate { it.copy(direction = value) }
    fun onStopLossChange(value: String) = _state.update { it.copy(stopLoss = value) }
    fun onTakeProfitChange(value: String) = _state.update { it.copy(takeProfit = value) }
    fun onQuantityChange(value: String) = _state.updateAndRecalculate { it.copy(quantity = value) }
    fun onRiskPercentChange(value: String) = _state.update { it.copy(riskPercent = value) }
    fun onStrategyChange(value: String) = _state.update { it.copy(strategy = value) }
    fun onNotesChange(value: String) = _state.update { it.copy(notes = value) }
    fun onExitPriceChange(value: String) = _state.updateAndRecalculate { it.copy(exitPrice = value) }

    fun toggleMistake(mistake: String) {
        _state.update {
            val updatedMistakes = if (it.mistakes.contains(mistake)) {
                it.mistakes - mistake
            } else {
                it.mistakes + mistake
            }
            it.copy(mistakes = updatedMistakes)
        }
    }

    fun loadTrade(id: String) {
        if (editingTradeId == id && originalTrade != null) return

        viewModelScope.launch {
            _state.update { it.copy(isLoadingTrade = true) }
            val trade = getTradeByIdUseCase(id)
            if (trade != null) {
                editingTradeId = trade.id
                originalTrade = trade
                _state.update {
                    it.copy(
                        tradeId = trade.id,
                        dateTime = trade.date.atStartOfDay(),
                        instrument = trade.instrument,
                        entryPrice = trade.entryPrice.toString(),
                        stopLoss = trade.stopLoss?.toString().orEmpty(),
                        takeProfit = trade.target?.toString().orEmpty(),
                        quantity = trade.quantity.toString(),
                        riskPercent = trade.riskPercent?.toString().orEmpty(),
                        strategy = trade.strategy.orEmpty(),
                        notes = trade.emotion.orEmpty(),
                        mistakes = trade.mistakes,
                        direction = trade.direction,
                        status = trade.status,
                        exitPrice = trade.exitPrice?.toString().orEmpty(),
                        isEditing = true,
                        pnlFormatted = formatPnl(trade.entryPrice, trade.quantity.toDouble(), trade.direction, trade.exitPrice),
                        isLoadingTrade = false
                    )
                }
            } else {
                _state.update { it.copy(isLoadingTrade = false) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTrade(onSaved: () -> Unit) {
        val current = _state.value
        if (!current.canSave) return

        viewModelScope.launch {
            val existing = originalTrade
            val entryPrice = current.entryPrice.toDoubleOrNull() ?: 0.0
            val exitPrice = current.exitPrice.toDoubleOrNull() ?: existing?.exitPrice
            val quantity = current.quantity.toIntOrNull() ?: 0
            val calculatedPnl = calculatePnl(
                entryPrice = entryPrice,
                quantity = quantity.toDouble(),
                direction = current.direction,
                exitPrice = exitPrice
            )
            val trade = Trade(
                id = existing?.id ?: UUID.randomUUID().toString(),
                date = existing?.date ?: current.dateTime.toLocalDate(),
                instrument = current.instrument,
                marketType = existing?.marketType ?: MarketType.FNO,
                direction = current.direction,
                entryPrice = entryPrice,
                exitPrice = exitPrice,
                quantity = quantity,
                stopLoss = current.stopLoss.toDoubleOrNull(),
                target = current.takeProfit.toDoubleOrNull(),
                riskPercent = current.riskPercent.toDoubleOrNull(),
                pnl = calculatedPnl ?: existing?.pnl,
                status = if ((current.exitPrice.toDoubleOrNull() != null) && current.status == TradeStatus.OPEN) TradeStatus.CLOSED else current.status,
                strategy = current.strategy.ifBlank { null },
                mistakes = current.mistakes,
                emotion = current.notes.ifBlank { null }
            )

            if (editingTradeId == null) {
                insertTradeUseCase(trade)
            } else {
                updateTradeUseCase(trade)
            }
            originalTrade = trade
            onSaved()
        }
    }

    fun closeTrade(exitPrice: Double, onClosed: () -> Unit) {
        val trade = originalTrade ?: return

        viewModelScope.launch {
            val pnl = when (trade.direction) {
                TradeDirection.BUY -> (exitPrice - trade.entryPrice) * trade.quantity
                TradeDirection.SELL -> (trade.entryPrice - exitPrice) * trade.quantity
            }

            val updatedTrade = trade.copy(
                exitPrice = exitPrice,
                pnl = pnl,
                status = TradeStatus.CLOSED
            )

            updateTradeUseCase(updatedTrade)
            originalTrade = updatedTrade
            _state.updateAndRecalculate {
                it.copy(
                    status = TradeStatus.CLOSED,
                    exitPrice = exitPrice.toString()
                )
            }
            onClosed()
        }
    }

    private fun MutableStateFlow<AddTradeState>.updateAndRecalculate(transform: (AddTradeState) -> AddTradeState) {
        update { current ->
            val next = transform(current)
            next.copy(
                pnlFormatted = formatPnl(
                    entryPrice = next.entryPrice.toDoubleOrNull(),
                    quantity = next.quantity.toDoubleOrNull(),
                    direction = next.direction,
                    exitPrice = next.exitPrice.toDoubleOrNull()
                )
            )
        }
    }

    private fun formatPnl(
        entryPrice: Double?,
        quantity: Double?,
        direction: TradeDirection,
        exitPrice: Double?
    ): String {
        val pnl = calculatePnl(entryPrice, quantity, direction, exitPrice) ?: return ""
        return "₹ %.2f".format(pnl)
    }

    private fun calculatePnl(
        entryPrice: Double?,
        quantity: Double?,
        direction: TradeDirection,
        exitPrice: Double?
    ): Double? {
        if (entryPrice == null || quantity == null || exitPrice == null) return null
        return when (direction) {
            TradeDirection.BUY -> (exitPrice - entryPrice) * quantity
            TradeDirection.SELL -> (entryPrice - exitPrice) * quantity
        }
    }
}

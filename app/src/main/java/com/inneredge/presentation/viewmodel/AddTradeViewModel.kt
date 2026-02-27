package com.inneredge.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inneredge.domain.model.MarketType
import com.inneredge.domain.model.Trade
import com.inneredge.domain.model.TradeDirection
import com.inneredge.domain.model.TradeStatus
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
    private val updateTradeUseCase: UpdateTradeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddTradeState())
    val state: StateFlow<AddTradeState> = _state.asStateFlow()

    fun onInstrumentChange(value: String) = _state.update { it.copy(instrument = value) }

    fun onEntryPriceChange(value: String) = _state.update {
        it.copy(entryPrice = value).withCalculatedPnl()
    }

    fun onDirectionChange(value: TradeDirection) = _state.update {
        it.copy(direction = value).withCalculatedPnl()
    }

    fun onStopLossChange(value: String) = _state.update { it.copy(stopLoss = value) }

    fun onTakeProfitChange(value: String) = _state.update { it.copy(takeProfit = value) }

    fun onQuantityChange(value: String) = _state.update {
        it.copy(quantity = value).withCalculatedPnl()
    }

    fun onRiskPercentChange(value: String) = _state.update { it.copy(riskPercent = value) }

    fun onStrategyChange(value: String) = _state.update { it.copy(strategy = value) }

    fun onNotesChange(value: String) = _state.update { it.copy(notes = value) }

    fun onExitPriceChange(value: String) = _state.update {
        it.copy(exitPrice = value).withCalculatedPnl()
    }

    fun toggleMistake(mistake: String) = _state.update {
        val updated = if (mistake in it.mistakes) it.mistakes - mistake else it.mistakes + mistake
        it.copy(mistakes = updated)
    }

    fun startEditing(trade: Trade) {
        _state.value = AddTradeState(
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
            isEditing = true,
            status = trade.status,
            exitPrice = trade.exitPrice?.toString().orEmpty(),
            direction = trade.direction,
            pnlFormatted = trade.pnl?.let { "₹ %.2f".format(it) }.orEmpty()
        ).withCalculatedPnl()
    }

    fun closeTrade() {
        _state.update {
            it.copy(status = TradeStatus.CLOSED).withCalculatedPnl()
        }
    }

    fun saveTrade(onSaved: () -> Unit) {
        val current = _state.value
        if (!current.canSave) return

        val trade = Trade(
            id = current.tradeId ?: UUID.randomUUID().toString(),
            date = current.dateTime.toLocalDate(),
            instrument = current.instrument,
            marketType = MarketType.FNO,
            direction = current.direction,
            entryPrice = current.entryPrice.toDoubleOrNull() ?: 0.0,
            exitPrice = current.exitPrice.toDoubleOrNull(),
            quantity = current.quantity.toIntOrNull() ?: 0,
            stopLoss = current.stopLoss.toDoubleOrNull(),
            target = current.takeProfit.toDoubleOrNull(),
            riskPercent = current.riskPercent.toDoubleOrNull(),
            pnl = current.pnlFormatted.removePrefix("₹").trim().toDoubleOrNull(),
            status = current.status,
            strategy = current.strategy.takeIf { it.isNotBlank() },
            mistakes = current.mistakes,
            emotion = current.notes.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            if (current.isEditing) {
                updateTradeUseCase(trade)
            } else {
                insertTradeUseCase(trade)
            }
            onSaved()
        }
    }

    private fun AddTradeState.withCalculatedPnl(): AddTradeState {
        val entry = entryPrice.toDoubleOrNull()
        val qty = quantity.toDoubleOrNull()
        val exit = exitPrice.toDoubleOrNull()

        val pnl = if (entry != null && qty != null && exit != null) {
            if (direction == TradeDirection.BUY) (exit - entry) * qty else (entry - exit) * qty
        } else {
            null
        }

        return copy(pnlFormatted = pnl?.let { "₹ %.2f".format(it) }.orEmpty())
    }
}

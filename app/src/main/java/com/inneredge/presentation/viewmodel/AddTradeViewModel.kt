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
import java.time.LocalDate
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

    fun onInstrumentChange(value: String) = _state.update { it.copy(instrument = value) }
    fun onEntryPriceChange(value: String) = _state.update { it.copy(entryPrice = value) }
    fun onDirectionChange(value: TradeDirection) = _state.update { it.copy(direction = value) }

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
                        instrument = trade.instrument,
                        entryPrice = trade.entryPrice.toString(),
                        direction = trade.direction,
                        status = trade.status,
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
            val trade = Trade(
                id = existing?.id ?: UUID.randomUUID().toString(),
                date = existing?.date ?: LocalDate.now(),
                instrument = current.instrument,
                marketType = existing?.marketType ?: MarketType.FNO,
                direction = current.direction,
                entryPrice = current.entryPrice.toDoubleOrNull() ?: 0.0,
                exitPrice = existing?.exitPrice,
                quantity = existing?.quantity ?: 50,
                stopLoss = existing?.stopLoss,
                target = existing?.target,
                riskPercent = existing?.riskPercent,
                pnl = existing?.pnl,
                status = existing?.status ?: TradeStatus.OPEN,
                strategy = existing?.strategy,
                mistakes = existing?.mistakes ?: emptyList(),
                emotion = existing?.emotion
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
            _state.update { it.copy(status = TradeStatus.CLOSED) }
            onClosed()
        }
    }
}

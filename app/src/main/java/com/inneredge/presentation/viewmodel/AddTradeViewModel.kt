package com.inneredge.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inneredge.domain.model.MarketType
import com.inneredge.domain.model.Trade
import com.inneredge.domain.model.TradeDirection
import com.inneredge.domain.model.TradeStatus
import com.inneredge.domain.usecase.InsertTradeUseCase
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
    private val insertTradeUseCase: InsertTradeUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AddTradeState())
    val state: StateFlow<AddTradeState> = _state.asStateFlow()

    fun onInstrumentChange(value: String) = _state.update { it.copy(instrument = value) }
    fun onEntryPriceChange(value: String) = _state.update { it.copy(entryPrice = value) }
    fun onDirectionChange(value: TradeDirection) = _state.update { it.copy(direction = value) }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTrade(onSaved: () -> Unit) {
        val current = _state.value
        if (!current.canSave) return

        viewModelScope.launch {
            insertTradeUseCase(
                Trade(
                    id = UUID.randomUUID().toString(),
                    date = LocalDate.now(),
                    instrument = current.instrument,
                    marketType = MarketType.FNO,
                    direction = current.direction,
                    entryPrice = current.entryPrice.toDoubleOrNull() ?: 0.0,
                    exitPrice = null,
                    quantity = 50,
                    stopLoss = null,
                    target = null,
                    riskPercent = null,
                    pnl = null,
                    status = TradeStatus.OPEN
                )
            )
            onSaved()
        }
    }
}

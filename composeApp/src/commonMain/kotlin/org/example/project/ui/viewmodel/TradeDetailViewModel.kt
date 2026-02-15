package org.example.project.ui.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.project.domain.model.Trade
import org.example.project.domain.model.TradeDirection
import org.example.project.domain.model.TradeStatus
import org.example.project.domain.repository.TradeRepository

class TradeDetailViewModel(
    private val repository: TradeRepository
) {
    private val _trade = MutableStateFlow<Trade?>(null)
    val trade: StateFlow<Trade?> = _trade.asStateFlow()

    suspend fun loadTrade(id: String) {
        _trade.value = repository.getTradeById(id)
    }

    suspend fun closeTrade(exitPrice: Double) {
        val currentTrade = _trade.value ?: return
        if (currentTrade.status != TradeStatus.OPEN) return

        val pnl = when (currentTrade.direction) {
            TradeDirection.BUY -> exitPrice - currentTrade.entryPrice
            TradeDirection.SELL -> currentTrade.entryPrice - exitPrice
        }
        val newStatus = if (pnl >= 0) TradeStatus.WIN else TradeStatus.LOSS

        val updatedTrade = currentTrade.copy(
            exitPrice = exitPrice,
            pnl = pnl,
            status = newStatus
        )
        repository.updateTrade(updatedTrade)
        _trade.value = updatedTrade
    }

    suspend fun deleteTrade() {
        _trade.value?.let { repository.deleteTrade(it.id) }
        _trade.value = null
    }
}

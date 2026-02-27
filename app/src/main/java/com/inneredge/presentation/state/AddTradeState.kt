package com.inneredge.presentation.state

import com.inneredge.domain.model.TradeDirection
import com.inneredge.domain.model.TradeStatus
import java.time.LocalDateTime

data class AddTradeState(
    val tradeId: String? = null,
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val instrument: String = "",
    val entryPrice: String = "",
    val stopLoss: String = "",
    val takeProfit: String = "",
    val quantity: String = "",
    val riskPercent: String = "",
    val strategy: String = "",
    val notes: String = "",
    val mistakes: List<String> = emptyList(),
    val pnlFormatted: String = "",
    val direction: TradeDirection = TradeDirection.BUY,
    val status: TradeStatus = TradeStatus.OPEN,
    val exitPrice: String = "",
    val isEditing: Boolean = false,
    val isLoadingTrade: Boolean = false
) {
    val canSave: Boolean
        get() = instrument.isNotBlank() && entryPrice.isNotBlank() && quantity.isNotBlank()
}

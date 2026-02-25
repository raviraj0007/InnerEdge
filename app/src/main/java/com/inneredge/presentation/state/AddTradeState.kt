package com.inneredge.presentation.state

import com.inneredge.domain.model.TradeDirection

data class AddTradeState(
    val instrument: String = "",
    val entryPrice: String = "",
    val direction: TradeDirection = TradeDirection.BUY
) {
    val canSave: Boolean get() = instrument.isNotBlank() && entryPrice.toDoubleOrNull() != null
}

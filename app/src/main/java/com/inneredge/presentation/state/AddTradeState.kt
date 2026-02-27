package com.inneredge.presentation.state

import com.inneredge.domain.model.TradeDirection
import com.inneredge.domain.model.TradeStatus

data class AddTradeState(
    val tradeId: String? = null,
    val instrument: String = "",
    val entryPrice: String = "",
    val direction: TradeDirection = TradeDirection.BUY,
    val status: TradeStatus = TradeStatus.OPEN,
    val isLoadingTrade: Boolean = false
) {
    val isEditing: Boolean get() = tradeId != null
    val canSave: Boolean get() = instrument.isNotBlank() && entryPrice.toDoubleOrNull() != null
}

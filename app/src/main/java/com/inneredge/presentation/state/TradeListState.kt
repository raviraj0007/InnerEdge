package com.inneredge.presentation.state

import com.inneredge.domain.model.Trade

data class TradeListState(
    val trades: List<Trade> = emptyList(),
    val selectedFilter: TradeFilter = TradeFilter.ALL,
    val isFabExpanded: Boolean = false
) {
    val filteredTrades: List<Trade>
        get() = trades.filter { trade ->
            when (selectedFilter) {
                TradeFilter.ALL -> true
                TradeFilter.OPEN -> trade.status.name == "OPEN"
                TradeFilter.CLOSED -> trade.status.name == "CLOSED"
                TradeFilter.WIN -> (trade.pnl ?: 0.0) > 0.0
                TradeFilter.LOSS -> (trade.pnl ?: 0.0) < 0.0
            }
        }
}

enum class TradeFilter(val label: String) { ALL("All"), OPEN("Open"), CLOSED("Closed"), WIN("Win"), LOSS("Loss") }
